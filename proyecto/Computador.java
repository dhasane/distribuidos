import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

// representa lo que correra en un computador, falta definir un mejor nombre

public class Computador extends Conector{

    // contiee una lista de las conexiones
    private Broker broker;
    private List<Pais> paises;


    Computador(int port)
    {
        this.paises = new ArrayList<Pais>();

        //                       yo que se como responder y por donde escucho
        this.broker = new Broker(this, port);
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
    public void respond(Connection c, String respuesta)
    {

    }

    public void disconnect(Connection c)
    {
        broker.eliminar(c);
    }

}
