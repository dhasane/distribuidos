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
import java.util.HashMap;

import virus.Computador;
import virus.Utils;

public class Broker extends Conector{

    private Logger LOGGER;

    private Computador cnt;
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos
    private Conexiones con;

    public Broker(Computador cnt, int serverPort, int umbral)
    {
        try
        {
            this.con = new Conexiones(this, serverPort, umbral);
            this.cnt = cnt;
            this.umbral = umbral;
            LOGGER = Utils.getLogger(this, this.getNombre());
        }
        catch(IOException ioe )
        {
            ioe.printStackTrace();
        }
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

                // si quedo un poco mas limpio que antes
                respuesta = this.con.send(
                    cliente,
                    new Mensaje(
                        Mensaje.weight,
                        "oiga su peso" // el contenido no importa en este caso
                    )
                );

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

        if ( abs(diferencia) >= this.umbral )
        {
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

                if( abs(poblacion) <= umbral && ( index == -1 || pais < paisMinimo) )
                {
                    paisMinimo = pais;
                    index = posicion;
                }
                posicion++;
            }

            Object obj = cnt.getObject(index);
            if (obj != null){
                enviar( cliente,
                    createMensaje(
                        Mensaje.add,
                        obj
                    )
                );
                // si ya le pase o recibi una clase, no tengo que preguntarle al resto
                return true;
            }
        }
        return false;
    }

}
