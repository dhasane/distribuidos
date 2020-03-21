import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Broker{

    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private Conector cnt;
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos

    public Broker(Conector cnt, int serverPort, int umbral)
    {
        try
        {
            this.listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            escucharConexionesEntrantes();
        }
        catch(IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

    // se balancea cada vez que se agrega o elimina un elemento
    private void balancear()
    {
        // tengo este nuevo peso
        // ustedes cuanto peso tienen?
        int miPeso = cnt.peso();
        this.clientes.forEach(cliente ->{

            // esta funcion seria para enviar un mensaje y esperar su respuesta
            int peso = cliente.sendRespond("oiga, paseme su peso");

            int diferencia = miPeso - peso;

            if ( diferencia < 0 ) diferencia *= -1;

            if ( diferencia >= umbral )
            {
                // tu que tienes un peso suficientemente distinto al mio(dentro de un umbral), te paso una de mis clases que te acerque lo mejor posible al promedio entre tu peso y mi peso
                Object obj = cnt.conseguirObjetoPeso(diferencia, umbral);

            }

        });
        // si ya le pase o recibi una clase, no tengo que preguntarle al resto
    }

    // el broker se quedara escuchando por conexiones entrantes
    private void escucharConexionesEntrantes()
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
        balancear();
        return true;
    }

    public synchronized boolean agregar(Connection c)
    {
        // no pueden haber repetidos
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            balancear();
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
