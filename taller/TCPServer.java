import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TCPServer extends Conector{

    private ServerSocket listenSocket;
    private List<Connection> clientes;

    public static void main(String[] args) {
        int serverPort = 7896;   // Puerto a usar
        new TCPServer( serverPort );
    }

    @Override
    public void respond(String respuesta){
        System.out.println( " el envio del cliente es: " + respuesta );
    }

    @Override
    public void disconnect(Connection c)
    {
        this.clientes.remove(c);
    }

    public TCPServer( int serverPort )
    {

        clientes = new ArrayList<Connection>();
        try{
            listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
        }catch(IOException io){
            io.printStackTrace();
        }

        escucharClientesEntrantes();

        // al minuto imprime los clientes que se han agregado a la lista
        while(true)
        {
            try{
                TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException ie ){
                ie.printStackTrace();
            }

            clientes.forEach( x -> System.out.println(x) );
        }
    }


    private void escucharClientesEntrantes()
    {
        // hilo que espera a que lleguen nuevos clientes
        new Thread( () -> {
            try{
                while(true) {
                    //Esperar en modo escucha al cliente
                    //Establecer conexion con el socket del cliente(Hostname, Puerto)

                    // Escucha nuevo cliente y agrega en lista

                    // a las operaciones que se hagan sobre 'clientes' probablemente se les deberia poner algun bloqueo,
                    // para que se pueda tener mayor seguridad al haber paralelismo
                    clientes.add( new Connection( (Conector) this, listenSocket.accept()) );
                }
            } catch(IOException e) {
                System.out.println("Listen socket:"+e.getMessage());
            }
        }).start();
    }
}
