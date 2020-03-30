import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// representa lo que correra en un computador, falta definir un mejor nombre

public class Computador extends Conector{

    // contiene una lista de las conexiones
    private Broker broker;
    private List<Pais> paises;


    Computador(int port, int umbral)
    {
        this.paises = new ArrayList<Pais>();

        //                       yo que se como responder y por donde escucho
        this.broker = new Broker(this, port, umbral);
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
        Utils.print( prt + " ( total : " + pesoTotal + " )" );
    }

    // a mi me pasa el tiempo y le digo al resto que tambien lo pasen
    public void step(int pasos)
    {
        receiveStep(pasos);
        this.broker.send(
            new Mensaje(
                Mensaje.step,
                pasos
            )
        );
    }

    public void stop()
    {
        this.paises.forEach( p -> {
            p.detener();
            this.broker.sendRandomAdd(p);
        });
        this.broker.detener();
    }

    public void receiveStep(int pasos)
    {
        this.paises.forEach( p -> {
            p.step(pasos);
        });
    }

    public void agregarPais(
            String nombre,
            int poblacion,
            int enfermos_iniciales,
            double posibilidad_viaje,
            double posibilidad_viaje_aereo,
            String[] vecinos,
            String[] vecinos_aereos
            )
    {
        Utils.print( broker.getNombre() + " agregando pais " + nombre );
        agregar(
            new Pais(
                this.broker,
                nombre,
                poblacion,
                enfermos_iniciales,
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
        try{
            InetAddress host = InetAddress.getByName(strcon);

            broker.agregar(
                new Connection(
                    this,
                    new Socket(host, port)
                )
            );
            broker.balancear();
        }
        catch(UnknownHostException uhe ){
            System.out.println("direccion no encontrada");
        }
        catch (IOException e){
            e.printStackTrace();
        }
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
        return this.paises.stream()
                          .map( p -> p.getPoblacion() )
                          .collect(Collectors.toList());
    }

    private synchronized void agregar(Pais p)
    {
        if (p != null)
        {
            this.paises.add(p);
            imprimir();
        }
    }

    private synchronized void eliminar(Pais p)
    {
        if (this.paises.contains(p))
        {
            p.detener();
            this.paises.remove(p);
            imprimir();
        }
    }

    @Override
    public Object getObject(int index)
    {
        PaisEnvio pe = null;
        if (0 <= index && index < this.paises.size())
        {
            Pais pa = this.paises.get(index);
            pa.detener();
            pe = new PaisEnvio( pa );
            eliminar( pa );
            Utils.print("moviendo " + pe.getNombre());
        }
        return pe;
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        Utils.print( this.broker.getNombre() + " mensaje entrante: " + respuesta.toString() );

        int tipo = respuesta.getTipo();

        switch(tipo)
        {
            case 0: // simple

                Utils.print("llego mensaje simple : ");

                // texto simple que no importa, o algo asi

                break;
            case 1: // request
                Object obj = answerRequest(respuesta);

                // se envia respond
                broker.send(
                    c,
                    new Mensaje(
                        Mensaje.respond,
                        obj
                    )
                );

                break;
            case 2: // respond

                // en teoria es para contestar un request
                // pero para esto ya hay una funcion que atrapa directamente
                // el respond

                // en teoria aca se deberia enviar un accept

                // el problema es que los accept por el momento no hacen nada
                // porque no hay algo asi como una "cola" de envios previos

                break;
            case 3: // accept

                // si se recibe no se reenvia lo anterior

                break;
            case 4: // add

                if (respuesta.getContenido().getClass() == PaisEnvio.class)
                {
                    agregar(
                        new Pais((PaisEnvio)respuesta.getContenido(), this.broker)
                    );
                }

                // en teoria aca se deberia enviar un accept

                break;
            case 5: // weight - piden el peso

                // weight es un request, entonces responde
                broker.send(
                    c,
                    new Mensaje(
                        Mensaje.respond,
                        this.peso()
                    )
                );
                break;

            case 6:
                if ( respuesta.getContenido().getClass() == Integer.class )
                {
                    receiveStep( (int) respuesta.getContenido() );
                }

                break;
        }
    }

    public Object answerRequest(Mensaje request)
    {
        if (request.getContenido().getClass() == String.class)
        {
        }
        return null;
    }

    @Override
    public void nuevaConexion(Connection c)
    {
        broker.agregar(c);
        broker.balancear();
    }

    @Override
    public void disconnect(Connection c)
    {
        broker.eliminar(c);
    }

    @Override
    public boolean local(String receptor, Mensaje mensaje)
    {
        if (mensaje.getContenido().getClass() == Viajero.class)
        {
            Viajero v = (Viajero)mensaje.getContenido();
            for( Pais pais: this.paises )
            {
                if (pais.getNombre() == receptor)
                {
                    pais.viajeroEntrante(v);
                    return true;
                }
            }
        }
        return false;
    }

}
