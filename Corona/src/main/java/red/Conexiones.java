package red;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import virus.Utils;

public class Conexiones extends Thread{

    private Logger LOGGER;

    private Conector cnt;

    // hilo/conexiones
    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private boolean continuar;

    // respuestas
    private Map<String , Respuesta> respuestas;
    private final int cantidad_intentos = 5;
    private final int tiempo_espera = 1; // tiempo de espera en segundos

    public Conexiones(Conector cnt, int serverPort)
    {
        try
        {
            this.respuestas = new HashMap<String, Respuesta>();
            this.listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            this.continuar = true;
            // escucharConexionesEntrantes();
            this.start();
            LOGGER = Utils.getLogger(this, this.getNombre());
        }
        catch(IOException ioe )
        {
            ioe.printStackTrace();
        }
    }

    public List<Connection> getClientes()
    {
        return this.clientes;
    }

    public String getNombre()
    {
        return this.listenSocket.getLocalPort() + "";
    }

    public synchronized void respuestasEliminar(Mensaje msg)
    {
        this.respuestas.remove(msg);
    }

    public synchronized void respuestasAgregar(String key)
    {
        this.respuestas.put(
            key,
            new Respuesta()
        );
    }

    // el broker se quedara escuchando por conexiones entrantes
    // hilo que espera a que lleguen nuevos clientes
    public void run()
    {
        try{
            while(continuar) {
                //Esperar en modo escucha al cliente
                //Establecer conexion con el socket del cliente(Hostname, Puerto)
                // Escucha nuevo cliente y agrega en lista
                cnt.nuevaConexion(
                        new Connection( this, listenSocket.accept() )
                );
            }
        } catch(IOException e) {
            System.out.println("Listen socket:"+e.getMessage());
        }
        finally
        {
            try{
                this.listenSocket.close();
            }
            catch(IOException e)
            {}

            LOGGER.log(Level.INFO, "cerrando puerto : " + this.getNombre() );
            this.clientes.forEach(c->c.detener());
        }
    }

    public void detener()
    {
        this.continuar = false;
        try{
            this.listenSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized boolean eliminar(Connection c)
    {
        if( !this.clientes.contains(c) )
        {
            return false;
        }
        this.clientes.remove(c);
        return true;
    }

    public synchronized boolean agregar(String strcon, int port)
    {
        try{
            InetAddress host = InetAddress.getByName(strcon);
            agregar(
                new Connection(
                    this,
                    new Socket(host, port)
                )
            );
        }
        catch(UnknownHostException uhe ){
            System.out.println("direccion no encontrada");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean agregar(Connection c)
    {
        // no pueden haber repetidos, no tendria sentido
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            cnt.mensajeSaludo(c);
            return true;
        }
        return false;
    }

    // envia a una conexion especifica
    public Mensaje send(Connection c, Mensaje data)
    {
        return enviar(c, data);
    }

    public void sendRandomAdd(Object obj)
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
    public void disconnect(Connection c)
    {
        eliminar(c);
    }

    // envia a todas las conexiones
    public void send(Mensaje data)
    {
        this.clientes.forEach( x -> {
            enviar(x,data);
        });
    }

    private double random( int inferior, int superior )
    {
        double val = Math.random();
        if ( val < 0 ) val *= -1;
        val *= (superior-inferior);
        return val;
    }

    // espera a que se responda un mensaje con id, en caso de no ser respondido en
    // tiempo_de_espera, termina la ejecucion
    // esto porque no se puede quedar esperando para siempre :v
    private Mensaje esperarRetornoRespuesta(String id, int tiempo_de_espera) throws TimeoutException
    {
        Mensaje valor = null;
        // esto sirve para cortar el funcionamiento de algo despues de que supere un tiempo limite
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            final Future<Mensaje> f = es.submit(() -> {

                Respuesta r = this.respuestas.get(id);
                synchronized(r)
                {
                    do{
                        r.wait();
                    }while(!r.estado());
                }

                Mensaje m = r.conseguirMensaje();
                respuestasEliminar(m);

                return m;
            });

            valor = f.get(tiempo_de_espera , TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            Utils.print("tiempo excedido en la espera de respuesta");
            new TimeoutException();
        } catch (ExecutionException e) {
            Utils.print("se ha interrumpido la ejecucion");
        } catch (InterruptedException e) {
            Utils.print("se ha interrumpido la espera de la respuesta");
        } finally {
            es.shutdown();
        }

        return valor;
    }

    private Mensaje enviar(Connection c, Mensaje data)
    {
        if (data.isRequest())
            respuestasAgregar(data.getId());
        // se envia el mensaje
        c.send(data);
        Utils.print("enviando : " + data.toString() );

        Mensaje retorno = null;
        boolean exitoso = true;

        // en caso de ser un request, se tiene que esperar a que llegue la respuesta
        if (data.isRequest())
        {
            int intentos = cantidad_intentos;
            boolean seguir = true;

            do{
                try{
                    retorno = esperarRetornoRespuesta(data.getId(), tiempo_espera);
                    // llegar aca significa que todo corrio adecuadamente
                    seguir = false;
                }
                catch(TimeoutException e)
                {
                    // se reduce la cantidad de intentos y se reenvia el mensaje
                    intentos --;
                    c.send(data);
                    Utils.print("re-enviando : " + data.toString() );
                }
            }while(seguir);
        }
        return retorno;
    }

    public void respond(Connection c, Mensaje data)
    {
        // pero esto me permite buscar reply
        // ignorar todos los demas mensajes de este mismo id

        if ( data.isRespond() )
        {
            Utils.print(" llega respuesta : " + data.toString() );

            Respuesta r = this.respuestas.getOrDefault(data.getId(), null);
            // en caso de ya haber recibido la respuesta, esta ya habra sido manejada
            // y sera null
            if (r != null)
            {
                Utils.print("es agregada");
                r.agregarRespuesta(data);;
                synchronized(r)
                {
                    r.notifyAll();
                }
            }
        }
        cnt.respond(c, data);
        return;
    }

}
