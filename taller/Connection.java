import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Connection extends Thread{

    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;

    private int tiempo_reintento = 5;

    public Connection (Socket aClientSocket) {
       try {
           clientSocket = aClientSocket;
           in  = new DataInputStream(clientSocket.getInputStream()); //Canal de entrada cliente
           out = new DataOutputStream(clientSocket.getOutputStream()); //Canal de salida cliente
           this.start(); //hilo
       } catch(IOException e){
           System.out.println("Connection:"+e.getMessage());
           e.printStackTrace();
       }
    }

    // escuchar info entrante
    public void run() {
        try {
            while (true)
            {
                // esto seria chevere ponerlo para envio de objetos genericos
                String data = in.readUTF(); //Datos desde cliente
                System.out.println( clientSocket.getPort() + " envio: " + data);
            }
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
            e.printStackTrace();
        } catch(IOException e){
            System.out.println("readline:"+e.getMessage());
            e.printStackTrace();
        } finally{
            try {
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    // enviar info
    public void send( String data )
    {
        boolean sent = false;
        do{
            try{
                out.writeUTF(data);
                sent = true;
            } catch(IOException e){
                System.out.println("readline:"+e.getMessage());
                e.printStackTrace();
                try{
                    // esperar y reenviar
                    TimeUnit.SECONDS.sleep(this.tiempo_reintento);
                }
                catch(InterruptedException ie ){
                    ie.printStackTrace();
                }
            }
        }while(!sent);
    }
}
