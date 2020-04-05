package red;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

import virus.Utils;

public class Broker extends Thread{

    private Logger LOGGER;

    private Conector cnt;
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos

    // hilo/conexiones
    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private boolean continuar;

    // respuestas
    private Map<String , Mensaje> respuestas;
    private final int cantidad_intentos = 5;
    private final int tiempo_espera = 5; // tiempo de espera en segundos

    public Broker(Conector cnt, int serverPort, int umbral)
    {
        try
        {
            this.respuestas = new HashMap<String, Mensaje>();
            this.listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            this.umbral = umbral;
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

    public String getNombre()
    {
        return this.listenSocket.getLocalPort() + "";
    }

    // anter era para estandarisar y manejar el id que se les ingresaba
    // pero siendo que use el date para crear el id, esto dejo de ser necesari o
    public Mensaje createMensaje(double tipo, Object contenido)
    {
        Mensaje m = new Mensaje(
            tipo,
            contenido
        );
        return m;
    }


    // se balancea cada vez que se agrega o elimina un elemento
    // tal vez seria mejor preguntarles a todos el promedio y despues si trabajar sobre eso
    public void balancear()
    {

        // si se balancea, puede volver a intentar balancear
        boolean terminar;

        // tengo este nuevo peso
        // ustedes cuanto peso tienen?
        do{
            terminar = false;
            int miPeso = cnt.peso();
            Mensaje respuesta = null;
            for( Connection cliente: this.clientes )
            {

                // si quedo un poco mas limpio que antes
                respuesta = enviar(
                    cliente,
                    createMensaje(
                        Mensaje.weight,
                        "oiga su peso" // el contenido no importa en este caso
                    )
                );

                // que no este vacio y que el contenido sea int
                if ( respuesta != null && respuesta.getContenido().getClass() == Integer.class )
                {
                    int peso = (int) respuesta.getContenido();
                    terminar |= balancearCliente(cliente, miPeso, peso);
                }

                // no revisa al resto de clientes
                if (terminar) break;
            }
            // en caso de haber podido balancear exitosamente, reintenta
            // ya que al haber balanceado, podria tener mas objetos para balancear
        }while(terminar);
        // esto se podria hacer con programacion dinamica, o algo asi, pero seria
        // ya demasiado esfuerzo
    }

    public synchronized void respuestasEliminar(Mensaje msg)
    {
        this.respuestas.remove(msg);
    }

    public synchronized void respuestasAgregar(String key, Mensaje msg)
    {
        this.respuestas.put(
            key,
            msg
        );
    }

    // saca el valor absoluto
    private int abs(int num) {
        return num < 0 ? num * -1 : num;
    }

    // balancear contra un solo cliente
    public boolean balancearCliente( Connection cliente, int miPeso, int peso ) {

        int diferencia = (miPeso - peso)/2;

        if ( abs(diferencia) >= this.umbral )
        {
            // tu que tienes un peso suficientemente distinto al mio(dentro de un umbral), te paso una de mis clases que te acerque lo mejor posible al promedio entre tu peso y mi peso

            List<Integer> pesos = cnt.pesoObjetos();

            int index = -1;
            int paisMinimo = 0;
            int poblacion;

            int posicion = 0;
            // TODO esto se podria, en vez de buscar solo uno, buscar todos
            // los objetos que se necesiten para quedar balanceados
            // aunque eso ya seria mas como con programacion dinamica, o algo asi
            for(Integer pais: pesos)
            {
                poblacion = diferencia - pais;

                if( abs(poblacion) <= umbral && ( index == -1 || pais < paisMinimo) )
                {
                    paisMinimo = pais;
                    index = posicion;
                }
                posicion++;
            }

            Object obj = cnt.getObject(index);
            if (obj != null){
                enviar( cliente,
                    createMensaje(
                        Mensaje.add,
                        obj
                    )
                );
                // si ya le pase o recibi una clase, no tengo que preguntarle al resto
                return true;
            }
        }
        return false;
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

    public boolean agregar(String strcon, int port)
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

    public synchronized void sendAware( String receptor, Mensaje mensaje )
    {
        boolean entregado = false;
        do{

            if( !cnt.local(receptor, mensaje) )
            {
                LOGGER = Utils.getLogger(this, "enviando " + mensaje.getContenido() + " a otro computador" );
                for( Connection c: this.clientes )
                {
                    // fue entregado si el mensaje de respuesta dice que fue agregado
                    Mensaje m = enviar( c, mensaje );
                    if ( m != null)
                    {
                        entregado |= (m.getSubType() == Mensaje.agregado) ;
                    }
                }
            }

        } while( !entregado );
    }

    // envia a una conexion especifica
    public void send(Connection c, Mensaje data)
    {
        enviar(c, data);
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

                do{
                    synchronized(this.respuestas)
                    {
                        this.respuestas.wait();
                    }
                }while( this.respuestas.getOrDefault(id, null) == null );

                Mensaje m = this.respuestas.getOrDefault(id, null);

                if ( m != null )
                {
                    // si ya consiguio el mensaje, lo quita
                    respuestasEliminar(m);
                }

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
        // se envia el mensaje
        c.send(data);

        Mensaje retorno = null;
        boolean exitoso = true;

        // en caso de ser un request, se tiene que esperar a que llegue la respuesta
        if (data.isRequest())
        {
            Mensaje valor = null;
            int intentos = cantidad_intentos;
            boolean seguir = true;

            do{
                try{
                    valor = esperarRetornoRespuesta(data.getId(), tiempo_espera);
                    // llegar aca significa que todo corrio adecuadamente
                    seguir = false;
                }
                catch(TimeoutException e)
                {
                    // se reduce la cantidad de intentos y se reenvia el mensaje
                    intentos --;
                    c.send(data);
                }
            }while(seguir);

            if ( valor != null )
            {
                // en caso de que la respuesta sea un nuevo request
                if (valor.isRequest())
                {
                    cnt.respond(c, valor);
                }
                retorno = valor;
            }
        }
        return retorno;
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
            createMensaje(
                Mensaje.add,
                obj
            )
        );
    }

    public void respond(Connection c, Mensaje data)
    {
        // pero esto me permite buscar reply
        // ignorar todos los demas mensajes de este mismo id

        if ( data.isRespond() )
        {
            Utils.print(" llega respuesta : " + data.toString() );
            respuestasAgregar(
                data.getId(),
                data
            );
            synchronized(this.respuestas)
            {
                this.respuestas.notifyAll();
            }
            // le dice a todos los hilos que esten esperando que escuchen
            // a ver si es lo que esperaban
        }
        cnt.respond(c, data);
        return;
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
}
