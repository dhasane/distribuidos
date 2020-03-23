import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        String prt = broker.getNombre() + " : ";

        for(Pais p: this.paises)
        {
            prt += p.toString() + ", ";
        }
        Utils.print( prt );
    }

    public void agregarPais(Pais p)
    {
        Utils.print( broker.getNombre() + " agregando pais " + p.getNombre());
        this.paises.add(p);
        imprimir();
        broker.balancear();
        imprimir();
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

    // retorna el pais que reduzca la diferencia dentro del umbral aceptable
    @Override
    public Object conseguirObjetoPeso(int diferencia,int umbral)
    {
        int poblacion;
        for(Pais pais: this.paises)
        {
            poblacion = diferencia - pais.getPoblacion();

            Utils.print( broker.getNombre() + " cambio seria :" + diferencia + " -> " + poblacion + "   umbral : " + umbral);

            if( poblacion <= umbral && 0 < poblacion )
            {
                this.paises.remove(pais);
                return pais;
            }
        }
        return null;
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        respuesta.print();

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
                    this.paises.add((Pais)respuesta.getContenido());
                    imprimir();
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
