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
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos
    private Conexiones con;
    private boolean continuar;

    private int tiempoDescanso = 30000; // cada 30 segundos

    public Broker(Computador cnt, int serverPort, int umbral)
    {
        this.con = new Conexiones(this, serverPort);
        this.cnt = cnt;
        this.umbral = umbral;
        LOGGER = Utils.getLogger(this, this.getNombre());
        this.continuar = true;
        this.start();
    }

    public void run()
    {
        while(this.continuar)
        {
            Utils.print("balanceando");
            balancear();
            try{
                Thread.sleep(this.tiempoDescanso);
            }
            catch(InterruptedException ie)
            {

            }
        }
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        LOGGER.log( Level.INFO, "mensaje entrante: " + respuesta.toString() );

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
            // esto aca no es realmente necesario, este tipo de mensaje es
            // mas para evitar reenviar mensajes

            // Utils.print("lega objetoooooooooo " + respuesta.toString());

            // en teoria aca se deberia enviar un accept
            // pero no los estoy manejando
        }

        // por el momento no se usan accept
    }

    public String getNombre()
    {
        return this.con.getNombre();
    }

    @Override
    public void nuevaConexion(Connection c)
    {
        this.con.agregar(c);
        balancear();
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
            Mensaje respuesta = null;
            for( Connection cliente: this.con.getClientes() )
            {

                respuesta = this.con.send(
                    cliente,
                    new Mensaje(
                        Mensaje.weight,
                        "oiga su peso" // el contenido no importa en este caso
                    )
                );

                Utils.print("el peso de " + cliente.getPort() + " es " + respuesta);

                // que no este vacio y que el contenido sea int
                if ( respuesta != null && respuesta.getContenido().getClass() == Integer.class )
                {
                    int peso = (int) respuesta.getContenido();
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

        // tu que tienes un peso suficientemente distinto al mio(dentro de un umbral), te paso una de mis clases que te acerque lo mejor posible al promedio entre tu peso y mi peso

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

            if( abs(poblacion) < diferencia && ( index == -1 || pais < paisMinimo) )
            {
                paisMinimo = pais;
                index = posicion;
            }
            posicion++;
        }

        Utils.print( "el objeto para enviar esta en el indice " + index );

        Object obj = cnt.getObject(index);
        if (obj != null){
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
