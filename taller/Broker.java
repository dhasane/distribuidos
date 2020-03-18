
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Broker{

    Map<Connection , List<String> > topicos;
    private List<Connection> clientes;

    public Broker()
    {
        topicos = new HashMap<Connection, List<String> >();
        clientes = new ArrayList<Connection>();
    }

    public synchronized boolean eliminar(Connection c)
    {
        if( !this.clientes.contains(c) )
        {
            return false;
        }
        this.topicos.remove(c);
        this.clientes.remove(c);
        return true;
    }

    public synchronized boolean agregar(Connection c)
    {
        // no pueden haber repetidos
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            this.topicos.put(
                    c,
                    new ArrayList<String>()
            );
            return true;
        }
        return false;
    }

    public synchronized boolean eliminarTopico(Connection c, String topico)
    {
        if( topicos.get(c) == null  )
        {
            return false;
        }
        topicos.get(c).remove(topico);
        return true;
    }

    public synchronized void agregarTopico(Connection c, String topico)
    {
        agregar(c);

        if ( topicos.get(c) == null )
        {
            topicos.put(
                    c,
                    new ArrayList<String>()
            );
        }
        topicos.get(c).add(topico);
    }

    public void print()
    {
        System.out.println();
        clientes.forEach( x -> {
            System.out.print(x + " : ");
            if ( this.topicos.get(x) != null )
            {
                this.topicos.get(x).forEach( y -> System.out.print(y + " "));
            }
            System.out.println();
        });
    }

    // envia a una conexion especifica
    public void send(Connection c, String data)
    {
        c.send(data);
    }

    // envia a todas las conexiones
    public void send(String data)
    {
        this.clientes.forEach( x -> {
            x.send(data);
        });
    }

    // envia a todas las conexiones de un topico especifico
    public void send(String topico, String data)
    {
        this.clientes.forEach( x -> {
            if( this.topicos.get(x).contains(topico) )
            {
                x.send(data);
            }
        });
    }
}
