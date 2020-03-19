import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Broker{

    private ServerSocket listenSocket;
    private List<Connection> clientes;

    public Broker(Conector cnt, int serverPort)
    {
        try
        {
            listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            clientes = new ArrayList<Connection>();
            escucharConexionesEntrantes(cnt);
        }
        catch(IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

    // el broker se quedara escuchando por conexiones entrantes
    private void escucharConexionesEntrantes(Conector cnt)
    {
        // hilo que espera a que lleguen nuevos clientes
        new Thread( () -> {
            try{
                while(true) {
                    //Esperar en modo escucha al cliente
                    //Establecer conexion con el socket del cliente(Hostname, Puerto)

                    // Escucha nuevo cliente y agrega en lista
                    agregar( new Connection( cnt, listenSocket.accept()) );
                }
            } catch(IOException e) {
                System.out.println("Listen socket:"+e.getMessage());
            }
        }).start();
    }

    public synchronized boolean eliminar(Connection c)
    {
        if( !this.clientes.contains(c) )
        {
            return false;
        }
        this.clientes.remove(c);
        return true;
    }

    public synchronized boolean agregar(Connection c)
    {
        // no pueden haber repetidos
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            return true;
        }
        return false;
    }

    public void print()
    {
        System.out.println();
        clientes.forEach( x -> {
            System.out.println(x + " : ");
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
}
