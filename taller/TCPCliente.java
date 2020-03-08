import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPCliente {

    public static void main (String args[]) { // args message and hostname
        int serverPort = 7896;
        Connection servConn = null;
        Socket s = null;
        String texto = args.length >= 1 ? args[0] : "ping";
        try{
            // si hay un segundo argumento, lo toma como la direccion, de lo contrario se conecta a localhost
            InetAddress host = args.length >=2 ? InetAddress.getByName(args[1]) : InetAddress.getLocalHost();
            System.out.println(host.toString());
            servConn = new Connection(new Socket(host, serverPort));
            servConn.send(texto);
        }
        catch(UnknownHostException uhe ){
            uhe.printStackTrace();
        }
        catch (IOException e){
            System.out.println("readline:"+e.getMessage());
        }
    }
}
