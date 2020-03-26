import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jdk.jshell.execution.Util;

public class Connection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket clientSocket;
    private Conector cnt;
    private int esperandoRespuesta;
    private List<Mensaje> respuestas;

    private int tiempo_reintento = 5;
    private int cantidad_reintentos = 5;

    public Connection (Conector conector, Socket aClientSocket) {
        this.esperandoRespuesta = 0;
        respuestas = new ArrayList<Mensaje>();
        cnt = conector;
        try {
            clientSocket = aClientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream()); //Canal de salida
            out.flush();
            in  = new ObjectInputStream(clientSocket.getInputStream()); //Canal de entrada
            this.start(); //hilo
        } catch(IOException e){
            System.out.println("Connection:"+e.getMessage());
            e.printStackTrace();
        }
    }

    public String getNombre()
    {
        return this.clientSocket.getPort() + "";
    }

    // escuchar info entrante
    public void run() {
        try {
            while (true)
            {
                Mensaje data = (Mensaje) in.readObject(); //Datos desde cliente
                if ( data.getTipo() == Mensaje.respond )
                {
                    this.respuestas.add(data);
                    this.esperandoRespuesta--;
                }
                else
                {
                    cnt.respond(this, data);
                }
            }
        } catch(ClassNotFoundException e){
            e.printStackTrace();
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
    public boolean send( Mensaje data )
    {
        boolean sent = false;
        int intentos = cantidad_reintentos ;
        do{
            try{
                out.writeObject(data);
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

    // envia un mensaje y espera respuesta, es bloqueante
    public Mensaje sendRespond( Mensaje mensaje )
    {
        Mensaje respuesta=null;
        send(mensaje);

        int respInicial = this.esperandoRespuesta;

        this.esperandoRespuesta ++;

        // aqui busca la respuesta que espera por id o algo asi
        while( respInicial < esperandoRespuesta ){
            try
            {
                // por alguna razon tiene que haber aqui una pausa
                // que si no, como que no sirve....
                TimeUnit.SECONDS.sleep(1);
            }
            catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }

        // esto puede que de un error por el orden,
        // pero por el momento lo voy a dejar asi :v
        respuesta = this.respuestas.get(0);
        this.respuestas.remove(0);

        return respuesta;
    }
}
