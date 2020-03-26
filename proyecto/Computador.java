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

    public void agregarPais(Pais p)
    {
        Utils.print( broker.getNombre() + " agregando pais " + p.getNombre());
        agregar(p);
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
            this.paises.remove(p);
            imprimir();
        }
    }

    @Override
    public Object getObject(int index)
    {
        Pais p = null;
        if (0 <= index && index < this.paises.size())
        {
            p = this.paises.get(index);
            eliminar(p);
        }
        return p;
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        Utils.print( this.broker.getNombre() + " mensaje entrante: " + respuesta.toString() );

        int tipo = respuesta.getTipo();

        switch(tipo)
        {
            case 0: // simple

                break;
            case 1: // request
                Object obj = answerRequest(respuesta);

                broker.send(
                    c,
                    new Mensaje(
                        Mensaje.respond,
                        obj
                    )
                );

                break;
            case 2: // respond

                break;
            case 3: // accept

                break;
            case 4: // add

                if (respuesta.getContenido().getClass() == Pais.class)
                {
                    agregar((Pais)respuesta.getContenido());
                }

                break;
        }
    }

    public Object answerRequest(Mensaje request)
    {
        if (request.getContenido().getClass() == String.class)
        {
            String pedido = (String)request.getContenido();

            if(pedido.equals("oiga su peso"))
            {
                return this.peso();
            }
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

}
