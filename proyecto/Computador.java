import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

// representa lo que correra en un computador, falta definir un mejor nombre

public class Computador extends Conector{

    // contiene una lista de las conexiones
    private Broker broker;
    private List<Pais> paises;


    Computador(int port)
    {
        this.paises = new ArrayList<Pais>();

        //                       yo que se como responder y por donde escucho
        this.broker = new Broker(this, port, 500);
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
        }
        catch(UnknownHostException uhe ){
            // uhe.printStackTrace();
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

    // retorna el pais que reduzca la diferencia dentro del umbral aceptable o
    // el menor pais, a pesar de que aun no se cumpla
    @Override
    public Object conseguirObjetoPeso(int diferencia,int umbral)
    {
        int poblacion;
        Pais paisRetorno = null;
        for(Pais pais: this.paises)
        {
            poblacion = pais.getPoblacion();

            if( diferencia - poblacion < umbral)
            {
                this.paises.remove(pais);
                return pais;
            }
            if( paisRetorno == null || poblacion < paisRetorno.getPoblacion() )
            {
                paisRetorno = pais;
            }
        }
        return paisRetorno;
    }

    @Override
    public void respond(Connection c, Mensaje respuesta)
    {

    }

    public void disconnect(Connection c)
    {
        broker.eliminar(c);
    }

}
