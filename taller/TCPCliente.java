import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPCliente {
    static Socket s;

    public static void main (String args[]) { // args message and hostname
        // String[] arg = new String[2];
        // Scanner input = new Scanner(System.in);
        int serverPort = 7896;
        s = null;
        String texto = args.length >= 1 ? args[0] : "ping";
        // System.out.println("Ingrese el nombre del host");
        // String host = input.nextLine();
        try{
            InetAddress host = InetAddress.getLocalHost();
            // InetAddress host = InetAddress.getByName("0.0.0.0") // o una ip asi
            System.out.println(host.toString());
            enviar( texto, host, serverPort);
        }
        catch(UnknownHostException uhe ){
            uhe.printStackTrace();
        }
        // input.close();
    }

   public static void enviar( String envio, InetAddress host, int serverPort )
   {
        try{
            s = new Socket(host, serverPort);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(envio);
            String data = in.readUTF();
            System.out.println("Leí: "+ data) ;
        }catch (UnknownHostException e){
            System.out.println("Socket:"+e.getMessage());
        }catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        }catch (IOException e){
            System.out.println("readline:"+e.getMessage());
        }finally {
            if(s!=null) try { s.close(); }catch (IOException e){
                System.out.println("close:"+e.getMessage());
            }
        }
   }
}
