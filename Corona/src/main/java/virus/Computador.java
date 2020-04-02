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
import envio.Viajero;
import virus.Utils;

// representa lo que correra en un computador, falta definir un mejor nombre

public class Computador extends Conector{

    // contiene una lista de las conexiones
    private Broker broker;
    private List<Pais> paises;
    private Logger LOGGER;

    // similar a un reloj, marca el maximo de steps que se pueden hacer
    private int maxStep;
    // al actualizarse, tambien actualiza los de los paises

    public Computador(int port, int umbral)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    detener();
                }
	    });
        this.paises = new ArrayList<Pais>();

        //                       yo que se como responder y por donde escucho
        this.broker = new Broker(this, port, umbral);
        this.maxStep = 0;
        LOGGER = Utils.getLogger(this, this.broker.getNombre());
    }

    public void imprimir()
    {
        String prt = broker.getNombre() + " -> ";

        int pesoTotal = 0;
        for(Pais p: this.paises)
        {
            prt += p.toString() + ", ";
            pesoTotal += p.getPoblacion();
        }
        LOGGER.log(Level.INFO,  prt + " ( total : " + pesoTotal + " )" );
        Utils.print( prt + " ( total : " + pesoTotal + " )" );
    }

    // a mi me pasa el tiempo y le digo al resto que tambien lo pasen
    public void step(int pasos)
    {
        // hay que ver una forma de agergar los pasos a nuevos paises, para que todos queden igual
        Utils.print("agregando pasos : " + pasos );
        receiveStep(pasos);
        this.broker.send(
            new Mensaje(
                Mensaje.step,
                pasos
            )
        );
    }

    public synchronized void detener()
    {
        Utils.print("desconectando computador : " + this.broker.getNombre() );
        LOGGER.log(Level.INFO, "desconectando computador : " + this.broker.getNombre() );

        this.paises.forEach( p -> {
            p.detener();
            this.broker.sendRandomAdd(new PaisEnvio(p));
            this.paises.remove(p);
        });
        this.broker.detener();
    }

    public synchronized void receiveStep(int pasos)
    {
        LOGGER.log( Level.INFO, "se agregan " + pasos + " pasos");
        this.maxStep += pasos;
        this.paises.forEach( p -> {
            LOGGER.log( Level.INFO, "se agrega " + pasos + " pasos a " + p.getNombre());
            p.step(pasos);
        });
    }

    public void agregarPais(
            String nombre,
            int poblacion,
            int enfermos_iniciales,
            double alta_vulnerabilidad,
            double aislamiento,
            double posibilidad_viaje,
            double posibilidad_viaje_aereo,
            String[] vecinos,
            String[] vecinos_aereos
            )
    {
        LOGGER.log(Level.INFO, "agregando pais " + nombre  );
        agregar(
            new Pais(
                this.broker,
                nombre,
                poblacion,
                enfermos_iniciales,
                alta_vulnerabilidad,
                aislamiento,
                posibilidad_viaje,
                posibilidad_viaje_aereo,
                vecinos,
                vecinos_aereos
            )
        );
        broker.balancear();
    }

    public void agregarConexion(String strcon, int port)
    {
        broker.agregar( strcon, port );
        broker.balancear();
    }

    @Override
    public void mensajeSaludo(Connection c)
    {
        this.broker.send(
            c,
            new Mensaje(
                Mensaje.saludo,
                this.maxStep
            )
        );
    }


    @Override
    public int peso()
    {
        int peso = 0;
        for( Pais pais: this.paises )
        {
            peso += pais.getPoblacion();
        }
        return peso;
    }

    @Override
    public List<Integer> pesoObjetos()
    {
        // TODO se podria en el peso tambien contar comunicaciones
        return this.paises.stream()
                          .map( p -> p.getPoblacion() )
                          .collect(Collectors.toList());
    }

    private synchronized void agregar(Pais p)
    {
        if (p != null)
        {
            int maxSteps = p.getMaxStep();
            LOGGER.log( Level.INFO, "agregando pais : " + p.getNombre() + " con " + maxSteps + " <-> " + this.maxStep );
            Utils.print( "agregando pais : " + p.getNombre() + " con " + maxSteps + " <-> " + this.maxStep );
            if( maxSteps < this.maxStep )
            {
                int diferencia =  this.maxStep - p.getMaxStep();
                LOGGER.log( Level.INFO, "actualizando " + p.getNombre() + " de " + maxSteps + " a " + diferencia + " pasos" );

                p.step(diferencia);
            }
            this.paises.add(p);
            imprimir();
        }
    }

    private synchronized boolean eliminar(Pais p)
    {
        if (this.paises.contains(p))
        {
            p.interrupt();
            this.paises.remove(p);
            imprimir();
            return true;
        }
        return false;
    }

    @Override
    public Object getObject(int index)
    {
        PaisEnvio pe = null;
        if (0 <= index && index < this.paises.size())
        {
            Pais pa = this.paises.get(index);
            if (eliminar( pa ))
            {
                pe = new PaisEnvio( pa );
                LOGGER.log( Level.INFO, "enviando pais : " + pe.getNombre() );
            }
        }
        return pe;
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        LOGGER.log( Level.INFO, "mensaje entrante: " + respuesta.toString() );

        if (respuesta.isSaludo())
        {
            if ( respuesta.getContenido().getClass() == Integer.class )
            {
                int stepsOtro = (int) respuesta.getContenido();
                Utils.print("nueva conexion recibida");

                if (this.maxStep < stepsOtro)
                {
                    this.step(stepsOtro - this.maxStep);
                }
                else if( this.maxStep > stepsOtro )
                {
                    this.broker.send(
                            new Mensaje(
                                Mensaje.step,
                                this.maxStep - stepsOtro
                            )
                    );
                }
                // si son iguales all gud

            }
        }
        else if(respuesta.isRequest())
        {
            double tipo = Mensaje.noAgregado;
            Object contenido = null;
            switch(respuesta.getSubType())
            {
                case 1: // add

                    if (respuesta.getContenido().getClass() == PaisEnvio.class)
                    {
                        tipo = Mensaje.agregado;
                        agregar(
                            new Pais((PaisEnvio)respuesta.getContenido(), this.broker)
                        );
                    }

                    break;
                case 2: // weight - piden el peso

                    // weight es un request, entonces responde
                    // answerRequest(c, this.peso());
                    tipo = Mensaje.respond;
                    contenido = this.peso();
                    break;

                case 3: // steps

                    if ( respuesta.getContenido().getClass() == Integer.class )
                    {
                        receiveStep( (int) respuesta.getContenido() );
                        tipo = Mensaje.agregado;
                    }

                    break;
                case 4: // llega viajero

                    if(  respuesta.getContenido().getClass() == Viajero.class )
                    {
                        Viajero v = (Viajero) respuesta.getContenido();
                        String destino = v.getDestino();
                        for( Pais p : this.paises)
                        {
                            String p_actual = p.getNombre();
                            if( p_actual.equals(destino) )
                            {
                                LOGGER.log( Level.INFO, "entra viajero : " + v.prt() );
                                p.viajeroEntrante(v);
                                tipo = Mensaje.agregado;
                            }
                        }
                    }
                    break;
            }

            broker.send(
                c,
                new Mensaje(
                    tipo,
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

    @Override
    public void nuevaConexion(Connection c)
    {
        broker.agregar(c);
        broker.balancear();
    }

    @Override
    public boolean local(String receptor, Mensaje mensaje)
    {
        if (mensaje.getContenido().getClass() == Viajero.class)
        {
            Viajero v = (Viajero)mensaje.getContenido();
            for( Pais pais: this.paises )
            {
                if (pais.getNombre().equals(receptor))
                {
                    pais.viajeroEntrante(v);
                    return true;
                }
            }
        }
        return false;
    }

}
