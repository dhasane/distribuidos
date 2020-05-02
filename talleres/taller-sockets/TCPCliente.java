import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPCliente extends Conector{

    private Connection servConn;

    public static void main(String args[]) { // args message and hostname
        int serverPort = 7896;
        try{
            // si hay un segundo argumento, lo toma como la direccion, de lo contrario se conecta a localhost
            InetAddress host = args.length >=1 ? InetAddress.getByName(args[1]) : InetAddress.getLocalHost();

            new TCPCliente(host, serverPort);
        }
        catch(UnknownHostException uhe ){
            uhe.printStackTrace();
        }
    }

    public TCPCliente(InetAddress host, int serverPort){
        servConn = null;

        Scanner input = new Scanner(System.in);
        System.out.println(host.toString());
        try{
            servConn = new Connection( this, new Socket(host, serverPort));

            System.out.println(" se puede hacer:");
            System.out.println(" [e]nvio     \t (e: topico: mensaje)");
            System.out.println(" [s]uscribir \t (s: topico)");
            System.out.println(" [i]mprimir  \t (i:)");
            System.out.println(" la parte de texto no tiene excepciones, entonces matara la conexion si el texto no cuadra");

            while (enviar(input.nextLine()));
        }
        catch (IOException e){
            System.out.println("readline:"+e.getMessage());
        }
        input.close();
    }

    boolean enviar(String data)
    {
        // aqui seria interesante convertir los datos a json o algo asi para poderlos enviar de forma relativamente generica
        return servConn.send(data);
    }

    @Override
    public void respond(Connection c,String respuesta){
        System.out.println( " el envio del servidor es: " + respuesta );
    }

    @Override
    public void disconnect(Connection c)
    {
        System.out.println("chao");
        System.exit(0);
    }
}
