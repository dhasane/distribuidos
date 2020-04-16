package red;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import envio.PaisEnvio;

import java.util.HashMap;

import virus.Computador;
import virus.Pais;
import virus.Utils;

public class Broker extends Conector{

    private Logger LOGGER;

    private Computador cnt;
    private Conexiones con;
    private boolean continuar;
    private Map<Connection, Integer> pesos;

    private String Nombre;

    private int tiempoDescanso = 30000; // cada 30 segundos

    public Broker(Computador cnt, int serverPort)
    {
        this.Nombre = "Broker:" + String.valueOf(serverPort);
        this.cnt = cnt;

        this.pesos = new HashMap<Connection, Integer>();
        this.con = new Conexiones(this, serverPort);

        LOGGER = Utils.getLogger(this, this.Nombre );

        this.continuar = true;
        this.start();
    }

    public void run()
    {
        while(this.continuar)
        {
            send(
                new Mensaje(
                    Mensaje.weight,
                    "oiga su peso"
                )
            );
            try{
                Thread.sleep(this.tiempoDescanso);
            }
            catch(InterruptedException ie)
            {

            }

            Utils.print("balanceando " + this.con.prt());
            balancear();
            Utils.print( this.cnt.imprimir() );

        }
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        LOGGER.log( Level.INFO, "mensaje entrante a broker: " + respuesta.toString() );
        // Utils.print( "mensaje entrante a broker: " + respuesta.toString() );

        if(respuesta.isRequest())
        {
            double tipo = Mensaje.noAgregado;
            Object contenido = null;
            switch(respuesta.getSubType())
            {
                case 1: // add

                    if (respuesta.getContenido().getClass() == PaisEnvio.class)
                    {
                        cnt.agregar(
                            new Pais((PaisEnvio)respuesta.getContenido())
                        );
                        tipo = Mensaje.agregado;
                    }

                    break;
                case 2: // weight - piden el peso

                    // weight es un request, entonces responde
                    // answerRequest(c, this.peso());
                    tipo = Mensaje.info;
                    contenido = cnt.peso();
                    break;
            }

            // mensaje escuchado
            this.con.send(
                c,
                new Mensaje(
                    tipo,
                    respuesta.getId(),
                    contenido
                )
            );
        }
        else if(respuesta.isRespond())
        {

            if (respuesta.getTipo() == Mensaje.info && respuesta.getContenido().getClass() == Integer.class)
            {
                Utils.print("se agrega el peso de " + c.getPort() + " en " + respuesta.getContenido() );
                this.pesos.put(
                    c,
                    (int) respuesta.getContenido()
                );
            }
            // esto aca no es realmente necesario, este tipo de mensaje es
            // mas para evitar reenviar mensajes

            // Utils.print("lega objetoooooooooo " + respuesta.toString());

            // en teoria aca se deberia enviar un accept
            // pero no los estoy manejando
        }

        // por el momento no se usan accept
    }

    @Override
    public String getNombre()
    {
        return this.Nombre;
    }

    @Override
    public void nuevaConexion(Connection c)
    {
        this.con.agregar(c);
    }

    public void send(Mensaje m)
    {
        this.con.send(m);
    }

    public void sendRandomAdd(Object obj)
    {
        this.con.sendRandomAdd(obj);
    }

    public void detener()
    {
        this.con.detener();
    }

    public void agregar(String strcon, int port)
    {
        this.con.agregar( strcon, port );
    }

    // se balancea cada vez que se agrega o elimina un elemento
    // tal vez seria mejor preguntarles a todos el promedio y despues si trabajar sobre eso
    private void balancear()
    {
        // si se balancea, puede volver a intentar balancear
        boolean terminar;

        // tengo este nuevo peso
        // ustedes cuanto peso tienen?
        do{
            terminar = false;
            int miPeso = cnt.peso();
            int peso;
            for( Connection cliente: this.con.getClientes() )
            {
                peso = this.pesos.getOrDefault(cliente, -1);
                // Utils.print("el peso de " + cliente.getPort() + " es " + peso);

                // que no este vacio y que el contenido sea int
                if ( peso != -1 )
                {
                    terminar |= balancearCliente(cliente, miPeso, peso);
                }

                // no revisa al resto de clientes
                if (terminar) break;
            }
            // en caso de haber podido balancear exitosamente, reintenta
            // ya que al haber balanceado, podria tener mas objetos para balancear
        }while(terminar);
        // esto se podria hacer con programacion dinamica, o algo asi, pero seria
        // ya demasiado esfuerzo
    }


    // saca el valor absoluto
    private int abs(int num) {
        return num < 0 ? num * -1 : num;
    }

    // balancear contra un solo cliente
    public boolean balancearCliente( Connection cliente, int miPeso, int peso ) {

        int diferencia = (miPeso - peso)/2;

        // tu que tienes un peso suficientemente distinto al mio, te paso una de mis clases que te acerque lo mejor posible al promedio entre tu peso y mi peso

        List<Integer> pesos = cnt.pesoObjetos();

        int index = -1;
        int paisMinimo = 0;
        int poblacion;

        int posicion = 0;
        // TODO esto se podria, en vez de buscar solo uno, buscar todos
        // los objetos que se necesiten para quedar balanceados
        // aunque eso ya seria mas como con programacion dinamica, o algo asi
        for(Integer pais: pesos)
        {
            poblacion = diferencia - pais;

            // Utils.print("nueva posible poblacion : " + poblacion + " siendo la actual : " + diferencia + " > " + poblacion );

            if( abs(poblacion) < abs(diferencia) && ( index == -1 || pais < paisMinimo) )
            {
                paisMinimo = pais;
                index = posicion;
            }
            posicion++;
        }

        // Utils.print( "el objeto para enviar esta en el indice " + index );

        Object obj = cnt.getObject(index);
        if (obj != null){
            // Utils.print( ((PaisEnvio) obj ).toString() );
            this.con.send(
                cliente,
                new Mensaje(
                    Mensaje.add,
                    obj
                )
            );
            // si ya le pase o recibi una clase, no tengo que preguntarle al resto
            return true;
        }
        return false;
    }
}
