import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TCPServer {

    private static ServerSocket listenSocket;
    private static List<Connection> clientes;

    public static void main(String[] args) {

            int serverPort = 7896;   // Puerto a usar
            clientes = new ArrayList<Connection>();

            try{
                listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            }catch(IOException io){
                io.printStackTrace();
            }

            // hilo que espera a que lleguen nuevos clientes
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while(true) {
                           //Esperar en modo escucha al cliente
                           //Establecer conexion con el socket del cliente(Hostname, Puerto)
                           clientes.add( new Connection(listenSocket.accept()) );
                        }
                    } catch(IOException e) {
                        System.out.println("Listen socket:"+e.getMessage());
                    }
                }
            }).start();

            // al minuto imprime los clientes que se han agregado a la lista
            try{
                TimeUnit.MINUTES.sleep(1);
            }
            catch(InterruptedException ie ){
                ie.printStackTrace();
            }

            clientes.forEach( x -> System.out.println(x) );


    }

}
