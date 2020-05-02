package red;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import virus.Init;
import virus.Utils;

public class Connection extends Thread{

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket clientSocket;

    private final Conexiones bro;

    private boolean continuar;

    public Connection (final Conexiones bro, final Socket aClientSocket) {
        this.bro = bro;
        try {
            clientSocket = aClientSocket;
            out = new ObjectOutputStream(clientSocket.getOutputStream()); //Canal de salida
            out.flush();
            in  = new ObjectInputStream(clientSocket.getInputStream()); //Canal de entrada
            this.continuar = true;
            this.start(); //hilo
        } catch(final IOException e) {
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
                final Object obj = in.readObject(); //Datos desde cliente

                if ( obj.getClass() == Mensaje.class )
                {
                    final Mensaje data = (Mensaje) obj;
                    bro.respond(this, data);
                }
            }
        } catch(final SocketException e) {
        } catch(final ClassNotFoundException e) {
            // e.printStackTrace();
        } catch(final EOFException e) {
            // System.out.println("EOF:"+e.getMessage());
        } catch(final IOException e) {
            // System.out.println("readline:"+e.getMessage());
        }
        finally{
            try {
                clientSocket.close();
            } catch (final IOException e) {
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
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }

    // enviar info
    public boolean send( final Mensaje data )
    {
        boolean sent = false;
        try{
            out.writeObject(data);
            sent = true;
        } catch(final SocketException e) {
            Utils.print("conexion cerrada");
        } catch(final IOException e) {
            System.out.println("readline:"+e.getMessage());
            e.printStackTrace();
        }
        // se le podria agregar tambien para que espere una respuesta
        // o algo asi como cnt.sent(), para que esa sea la clase que elija como responder
        // o agregar una lista de identificadores de lo que se ha enviado, y que en escuchar(run) reciba cierto 'comando' para decir 'listo el receptor recibio el mensaje'
        return sent;
    }

	public static void main(final String args[]) {
	    if (args.length > 1)
	    {
	        final String config = args[0];
	        final String nombre = args[1];
	        Utils.print("iniciando con archivo de configuracion : " + config + " con nombre : " + nombre);
	        new Init(config, nombre);
	    } else {
	        Utils.print("por favor espeficiar archivo de configuracion y nombre");
	    }
	}
}
