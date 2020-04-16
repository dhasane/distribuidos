package red;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jdk.jshell.execution.Util;
import virus.Utils;

public class Connection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket clientSocket;
    private int esperandoRespuesta;
    private List<Mensaje> respuestas;

    private int tiempo_reintento = 5;
    private int cantidad_reintentos = 5;

    private Conexiones bro;

    private boolean continuar;

    public Connection (Conexiones bro, Socket aClientSocket) {
        this.esperandoRespuesta = 0;
        respuestas = new ArrayList<Mensaje>();
        this.bro = bro;
        try {
            clientSocket = aClientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream()); //Canal de salida
            out.flush();
            in  = new ObjectInputStream(clientSocket.getInputStream()); //Canal de entrada
            this.continuar = true;
            this.start(); //hilo
        } catch(IOException e) {
            System.out.println("Connection:"+e.getMessage());
            e.printStackTrace();
        }
    }

    public String getAddr()
    {
        return this.clientSocket.getInetAddress().toString();
    }

    public int getPort()
    {
        return this.clientSocket.getPort();
    }

    public String getNombre()
    {
        return this.clientSocket.getPort() + "";
    }

    // escuchar info entrante
    public void run() {
        try {
            while (continuar)
            {
                // Mensaje data = (Mensaje) in.readObject(); //Datos desde cliente
                Object obj = in.readObject(); //Datos desde cliente

                if ( obj.getClass() == Mensaje.class )
                {
                    Mensaje data = (Mensaje) obj;
                    bro.respond(this, data);
                }
            }
        } catch(SocketException e) {
        } catch(ClassNotFoundException e) {
            // e.printStackTrace();
        } catch(EOFException e) {
            // System.out.println("EOF:"+e.getMessage());
        } catch(IOException e) {
            // System.out.println("readline:"+e.getMessage());
        }
        finally{
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.print("conexion cerrada");
        bro.disconnect(this);
    }

    public synchronized void detener()
    {
        this.continuar = false;
        try{
            this.clientSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // enviar info
    public boolean send( Mensaje data )
    {
        boolean sent = false;
        int intentos = cantidad_reintentos ;
        try{
            out.writeObject(data);
            sent = true;
        } catch(SocketException e) {
            Utils.print("conexion cerrada");
        } catch(IOException e) {
            System.out.println("readline:"+e.getMessage());
            e.printStackTrace();
        }
        // se le podria agregar tambien para que espere una respuesta
        // o algo asi como cnt.sent(), para que esa sea la clase que elija como responder
        // o agregar una lista de identificadores de lo que se ha enviado, y que en escuchar(run) reciba cierto 'comando' para decir 'listo el receptor recibio el mensaje'
        return sent;
    }
}
