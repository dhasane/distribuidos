
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Broker{

    Map<String, List<Connection> > conexiones;

    Broker()
    {
        conexiones = new HashMap<String, List<Connection> >();
    }

    public void agregar( String topico, Connection cc){
        // en caso de que no exista, crea el topico
        if (conexiones.get(topico) == null )
        {
            conexiones.put(
                    topico,
                    new ArrayList<Connection>()
            );
        }
        conexiones.get(topico).add(cc);
    }

    public void send( String topico, String data )
    {
        if (conexiones.get(topico) != null )
            conexiones.get(topico).forEach( c -> c.send(data) );
    }


}
