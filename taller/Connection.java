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
    private Conector cnt;

    private int tiempo_reintento = 5;
    private int cantidad_reintentos = 5;

    public Connection (Conector conector, Socket aClientSocket) {
        cnt = conector;
        try {
            clientSocket = aClientSocket;
            in  = new DataInputStream(clientSocket.getInputStream()); //Canal de entrada
            out = new DataOutputStream(clientSocket.getOutputStream()); //Canal de salida
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
                cnt.respond(this, data);
            }
        } catch (EOFException e){
            System.out.println("EOF:"+e.getMessage());
        } catch(IOException e){
            System.out.println("readline:"+e.getMessage());
        } finally{
            try {
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            finally{
                cnt.disconnect(this);
            }
        }
    }

    // enviar info
    public boolean send( String data )
    {
        boolean sent = false;
        int intentos = cantidad_reintentos ;
        do{
            try{
                out.writeUTF(data);
                sent = true;
            } catch(IOException e){
                if (intentos == 0)
                    return sent;
                intentos --;

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
            // se le podria agregar tambien para que espere una respuesta
            // o algo asi como cnt.sent(), para que esa sea la clase que elija como responder
            // o agregar una lista de identificadores de lo que se ha enviado, y que en escuchar(run) reciba cierto 'comando' para decir 'listo el receptor recibio el mensaje'
        }while(!sent);
        return sent;
    }
}
