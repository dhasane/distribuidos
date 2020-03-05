import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private static ServerSocket listenSocket;

    public static void main(String[] args) {

        try{
            int serverPort = 7896;   // Puerto a usar
            listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto

            while(true) {
               Socket clientSocket = listenSocket.accept(); //Esperar en modo escucha al cliente
               new Connection(clientSocket); //Establecer conexion con el socket del cliente(Hostname, Puerto)
            }

        } catch(IOException e) {
            System.out.println("Listen socket:"+e.getMessage());
        }

    }

}
