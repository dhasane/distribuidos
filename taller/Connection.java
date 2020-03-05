import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class Connection extends Thread{

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public Connection (Socket aClientSocket) {
       try {
           clientSocket = aClientSocket;
           in  = new DataInputStream(clientSocket.getInputStream()); //Canal de entrada cliente
           out = new DataOutputStream(clientSocket.getOutputStream()); //Canal de salida cliente
           this.start(); //hilo
       } catch(IOException e){
           System.out.println("Connection:"+e.getMessage());
       }
    }

    public void run() {
        try {
            String data = in.readUTF(); //Datos desde cliente
            System.out.println("Leí " + data);
            out.writeUTF("pong"); //Datos para el cliente
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch(IOException e){
            System.out.println("readline:"+e.getMessage());
        } finally{
            try {
                clientSocket.close();
            }catch (IOException e)
            {}
        }
     }

    public void send( String data )
    {
        try{
            out.writeUTF(data);
        } catch(IOException e){
            System.out.println("readline:"+e.getMessage());
        }
    }

}
