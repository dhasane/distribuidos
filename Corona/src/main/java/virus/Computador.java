package virus;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import red.Broker;
import red.Conector;
import red.Mensaje;
import red.Connection;
import envio.PaisEnvio;
import virus.Utils;

// representa lo que correra en un computador, falta definir un mejor nombre
public class Computador {

    private Broker broker;
    private List<Pais> paises;
    private Logger LOGGER;

    public Computador(int port)
    {
        // por el momento, para facilitar las pruebas
        // Runtime.getRuntime().addShutdownHook(new Thread() {
        //         public void run() {
        //             detener();
        //         }
		// });
        this.paises = new ArrayList<Pais>();

        this.broker = new Broker(this, port);
        LOGGER = Utils.getLogger(this, this.broker.getNombre());
    }

    // imprime todos los paises contenidos en este computador
    public String imprimir()
    {
        String prt = broker.getNombre() + " -> ";
        int pesoTotal = 0;
        for(Pais p: this.paises)
        {
            prt += p.toString() + ", ";
            pesoTotal += p.getPoblacion();
        }
        String ret = "=====>" + prt + " ( total : " + pesoTotal + " )";
        LOGGER.log( Level.INFO, ret );
        return ret;
    }

    // detiene el funcionamiento
    public synchronized void detener()
    {
        // TODO verificar el funcionamiento de esto
        Utils.print("desconectando computador : " + this.broker.getNombre() );
        LOGGER.log(Level.INFO, "desconectando computador : " + this.broker.getNombre() );

        this.paises.forEach( p -> {
            this.broker.sendRandomAdd(eliminar(p));
        });
        this.broker.detener();
    }

    public void agregarPais(
            String nombre,
            int poblacion,
            int enfermos_iniciales,
            double alta_vulnerabilidad,
            double aislamiento,
            List<String[]> vecinos,
            int port
            )
    {
        LOGGER.log(Level.INFO, "agregando pais " + nombre  );
        agregar(
            new Pais(
                nombre,
                poblacion,
                enfermos_iniciales,
                alta_vulnerabilidad,
                aislamiento,
                vecinos,
                port
            )
        );
    }

    // agrega una nueva conexion
    public void agregarConexion(String strcon, int port)
    {
        broker.agregar( strcon, port );
    }

    // retorna la carga de este computador
    public int peso()
    {
        int peso = 0;
        for( Pais pais: this.paises )
        {
            peso += pais.getPoblacion();
        }
        return peso;
    }

    // retorna una lista con el peso de procesamiento de cada uno de los objetos
    public List<Integer> pesoObjetos()
    {
        // TODO se podria en el peso tambien contar comunicaciones
        return this.paises.stream()
                          .map( p -> p.getPoblacion() )
                          .collect(Collectors.toList());
    }

    // agrega un pais a la lista de paises
    public synchronized void agregar(Pais p)
    {
        boolean encontrado = false;
        for(Pais pa: this.paises)
        {
            encontrado |= pa.getNombre() == p.getNombre();
        }

        if (p != null && !encontrado)
        {
            LOGGER.log( Level.INFO, "agregando pais : " + p.getNombre() );
            Utils.print( "agregando pais : " + p.getNombre() );
            this.paises.add(p);
            Utils.print( imprimir() );
        }
    }

    // elimina un pais de la lista de paises
    private synchronized PaisEnvio eliminar(Pais p)
    {
        if (this.paises.contains(p))
        {
            PaisEnvio pe = p.detener();
            this.paises.remove(p);
            Utils.print( imprimir() );
            return pe;
        }
        return null;
    }

    // consigue un objeto y lo borra
    public synchronized Object getObject(int index)
    {
        PaisEnvio pe = null;
        if (0 <= index && index < this.paises.size())
        {
            Pais pa = this.paises.get(index);
            pe = eliminar( pa );
        }
        return pe;
    }
}
