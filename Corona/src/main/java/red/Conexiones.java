package red;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import virus.Utils;

public class Conexiones extends Thread{

    private Logger LOGGER;

    // receptor final de los mensajes
    private Conector cnt;

    // hilo/conexiones
    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private boolean continuar;

    // reintentos/reenvios
    private final int cantidad_intentos = 5;
    private final int tiempo_espera = 1; // tiempo de espera en segundos para las respuestas en segundos

    // respuestas
    private Map<String , Boolean> respuestas;
    private List<Respuesta> limpiar; // respuestas a limpiar
    private Thread limpieza; // hilo que se encarga de la limpieza de respuestas

    public Conexiones(final Conector cnt, final int serverPort)
    {
        try
        {
            this.limpieza = null;

            this.listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            this.continuar = true;
            LOGGER = Utils.getLogger(this, this.getNombre() + ":" + cnt.getNombre());
            this.start();
        } catch(final IOException ioe ) {
            ioe.printStackTrace();
        }
    }

    public Conexiones(final Conector cnt)
    {
        try
        {
            this.listenSocket = new ServerSocket(0); //Inicializar socket un puerto cualquiera
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            this.continuar = true;
            LOGGER = Utils.getLogger(this, this.getNombre());
            this.start();
        } catch(final IOException ioe ) {
            ioe.printStackTrace();
        }
    }

    public List<Connection> getClientes()
    {
        return this.clientes;
    }

    public int getPort()
    {
        return this.listenSocket.getLocalPort();
    }

    public String getNombre()
    {
        return this.listenSocket.getLocalPort() + "";
    }

    // hilo que espera a que lleguen nuevos clientes
    public void run()
    {
        try{
            while(continuar) {
                // Esperar en modo escucha al cliente
                // Establecer conexion con el socket del cliente(Hostname, Puerto)
                // Escucha nuevo cliente y agrega en lista
                cnt.nuevaConexion(
                        new Connection( this, listenSocket.accept() )
                );
            }
        } catch(final IOException e) {
            System.out.println("Listen socket:"+e.getMessage());
        }
        finally
        {
            try{
                this.listenSocket.close();
            } catch(final IOException e) {}

            LOGGER.log(Level.INFO, "cerrando puerto : " + this.getNombre() );
        }
    }

    public synchronized void detener()
    {
        this.continuar = false;
        LOGGER.log(Level.INFO, "deteniendo conexiones " );
        this.limpieza.interrupt();
        try{
            this.listenSocket.close();
        } catch(final IOException e) {
            e.printStackTrace();
        }
        synchronized(this.clientes)
        {
            this.clientes.forEach(c->c.detener());
        }
    }

    public synchronized boolean eliminar(final Connection c)
    {
        if( !this.clientes.contains(c) )
        {
            return false;
        }
        LOGGER.log(Level.INFO, "cerrando conexion : " + c.getAddr() + ":" + c.getPort() );
        this.clientes.remove(c);
        return true;
    }

    public List<String[]> getConexiones()
    {
        final List<String[]> conexiones = new ArrayList<String[]>();
        for( final Connection c: this.clientes )
        {
            final String[] val = { c.getAddr(), String.valueOf(c.getPort()) };
            conexiones.add(val);
        }
        return conexiones;
    }

    public synchronized boolean agregar(final String strcon, final int port)
    {
        boolean ret = false;
        try{
            final InetAddress host = InetAddress.getByName(strcon);
            ret = agregar(
                new Connection(
                    this,
                    new Socket(host, port)
                )
            );
        } catch(final ConnectException ce) {
            // Utils.print("conexion : " + strcon + ":" + port + " no disponible" );
        } catch(final UnknownHostException uhe ) {
            // Utils.print("direccion no encontrada");
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public synchronized boolean agregar(final Connection c)
    {
        // no pueden haber repetidos, no tendria sentido
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            return true;
        }
        return false;
    }

    // retorna un string para imprimir
    public synchronized String prt()
    {
        String prt = this.clientes.size() + " > [";
        boolean primero = true;
        for( final Connection c : this.clientes )
        {
            if (primero)
            {
                primero = false;
            } else {
                prt += ", ";
            }
            prt += c.getAddr() + ":" + c.getPort();
        }
        return prt + "]";
    }

    public String[] getAddr()
    {
        final String[] addr = new String[2];
        addr[0] = this.listenSocket.getInetAddress().getHostAddress().toString();
        addr[1] = String.valueOf( this.listenSocket.getLocalPort() );

        return addr;
    }

    public void sendRandomAdd(final Object obj)
    {
        // si no hay nadie a quien enviarle los objetos, nada que hacer
        if(this.clientes.isEmpty())
            return;

        // de lo contrario lo envia a una conexion aleatoria, con el fin de no
        // perder el objeto al desconectar este Conector
        enviar(
            this.clientes.get((int) random(0,this.clientes.size())),
            new Mensaje(
                Mensaje.add,
                obj
            )
        );
    }

    // evento de desconexion de un socket
    public void disconnect(final Connection c)
    {
        eliminar(c);
    }

    private double random( final int inferior, final int superior )
    {
        double val = Math.random();
        if ( val < 0 ) val *= -1;
        val *= (superior-inferior);
        return val;
    }

    // envia a una conexion especifica
    public synchronized void send(final Connection c, final Mensaje data)
    {
        enviar(c, data);
    }

    // envia a todas las conexiones
    public synchronized void send(final Mensaje data)
    {
        // tambien se podria retornar una lista de respuestas
        // aunque por el momento no es necesario
        for (final Connection c : this.clientes)
        {
            enviar(c,data);
        }
    }

    private synchronized void enviar(final Connection c, final Mensaje data)
    {
        c.send(data);

        // en caso de ser un request, se tiene que esperar a que llegue la respuesta
        if (data.isRequest())
        {
            // reintentar mientras no llegue respuesta, sin tener que bloquear el resto del funcionamiento
            new Thread(()->{
                int intentos = cantidad_intentos;
                final boolean seguir = true;

                do{
                    intentos --;
                    c.send(data);
                    try{
                        // esperarRetornoRespuesta(data.getId(), tiempo_espera, c);
                        Thread.sleep(tiempo_espera*1000);
                        // llegar aca significa que todo corrio adecuadamente
                    } catch(final InterruptedException ie){

                    }
                }while( !conseguirRespuesta(data.getId()) && intentos > 0);
            }).start();
        }
    }

    private void crearRespuestas()
    {
        if(this.respuestas == null)
        {
            this.respuestas = new HashMap<String, Boolean>();
            this.limpiar = new ArrayList<Respuesta>();
        }
    }

    private boolean conseguirRespuesta(final String id)
    {
        crearRespuestas();
        return this.respuestas.getOrDefault(id, false);
    }

    private synchronized void eliminarRespuesta()
    {
        if ( respuestas.size() > 0 )
        {
            this.respuestas.remove( this.limpiar.get(0) );
            this.limpiar.remove(0);
        }
    }

    private void limpiarRespuestas()
    {
        if ( this.limpieza == null || !this.limpieza.isAlive() )
        {
            this.limpieza = new Thread( () -> {

                // se deben ir limpiando cada cierto tiempo, para permitir que sirvan como filtro para muchos paquetes que lleguen iguales, pero que no se quede llenando espacio eternamente
                while(!this.limpiar.isEmpty())
                {
                    try{
                        Thread.sleep( this.limpiar.get(0).getTiempo() );
                        eliminarRespuesta();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }

            });
        }
        // de lo contrario ya esta corriendo
    }

    private synchronized void contestar(final String id)
    {
        crearRespuestas();

        this.respuestas.put(id, true);
        // agregar un hilo que espere mientras va limpiando el mapa
        int tiempo_espera_eliminar = 5000;

        if ( !this.limpiar.isEmpty() )
        {
            // si un valor entra, se pondra 5000, si un segundo valor entra
            for( final Respuesta r : this.limpiar )
            {
                tiempo_espera_eliminar -= r.getTiempo();
                if (tiempo_espera_eliminar < 0 )
                {
                    tiempo_espera_eliminar = 0;
                    break;
                }
            }
        }

        this.limpiar.add( new Respuesta(id,tiempo_espera_eliminar) );
        limpiarRespuestas(); // intenta lanzar la limpieza
    }

    public void respond(final Connection c, final Mensaje data)
    {
        // si respuestas es igual a null, no importan las respuestas que lleguen
        if( conseguirRespuesta(data.getId()) )
            return;

        limpiarRespuestas();

        // pero esto me permite buscar reply
        // ignorar todos los demas mensajes de este mismo id
        if ( data.isRespond() )
        {
            // en caso de ya haber recibido la respuesta, esta ya habra sido manejada
            // y sera null
                // Utils.print("mensaje a conector");
            contestar(data.getId());
            cnt.respond(c, data);
            return ;
        } else {
            contestar(data.getId());
            cnt.respond(c, data);
        }
        return;
    }
}
