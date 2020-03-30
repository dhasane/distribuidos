import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TCPServer extends Conector{

    private ServerSocket listenSocket;
    private Broker clientes;

    public static void main(String[] args) {
        // Puerto a usar
        int serverPort = args.length >=1 ? Integer.parseInt(args[1]) : 7896;
        new TCPServer( serverPort );
    }

    @Override
    public void respond(Connection c, String respuesta){

        if (respuesta.contains("s:"))
        {
            System.out.println("suscrito");
            this.clientes.agregarTopico(c, respuesta.split(":")[1].trim());
        }

        if (respuesta.contains("e:"))
        {
            System.out.println("enviando");
            this.clientes.send(respuesta.split(":")[1].trim(), respuesta.split(":")[2].trim());
        }

        if (respuesta.contains("i:"))
        {
            System.out.println("imprimiendo");
            this.clientes.print();
        }
    }

    @Override
    public void disconnect(Connection c)
    {
        clientes.eliminar(c);
    }

    public TCPServer( int serverPort )
    {
        try{
            listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto

            clientes = new Broker();

            escucharClientesEntrantes();

            // imprime los cada 10 segundos clientes que se han agregado a la lista
            // while(true)
            // {
            //     try{
            //         TimeUnit.SECONDS.sleep(10);
            //     }
            //     catch(InterruptedException ie ){
            //         ie.printStackTrace();
            //     }
            //     clientes.print();
            // }
        }catch(IOException io){
            io.printStackTrace();
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
                    clientes.agregar( new Connection( this, listenSocket.accept()) );
                }
            } catch(IOException e) {
                System.out.println("Listen socket:"+e.getMessage());
            }
        }).start();
    }
}
