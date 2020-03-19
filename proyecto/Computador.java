import java.util.ArrayList;
import java.util.List;

// representa lo que correra en un computador, falta definir un mejor nombre

public class Computador extends Conector{

    // contiee una lista de las conexiones
    private Broker broker;
    private List<Pais> paises;

    public static void main(String args[]) {

    }

    Computador()
    {
        this.paises = new ArrayList<Pais>();

        //                       yo que se como responder y por donde escucho
        this.broker = new Broker(this, 5432);
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
