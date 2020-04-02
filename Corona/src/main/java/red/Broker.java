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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

import virus.Utils;

public class Broker extends Thread{

    private Logger LOGGER;
    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private Conector cnt;
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos

    private boolean continuar;

    private Map<String , Mensaje> respuestas;
    private Map<String , Boolean> esperas;

    private int tiempoDescanso = 1500; // segundo y medio

    private final int tiempo_espera = 2;

    public Broker(Conector cnt, int serverPort, int umbral)
    {
        try
        {
            this.respuestas = new HashMap<String, Mensaje>();
            this.esperas = new HashMap<String, Boolean>();
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
        boolean continuarBalanceo;

        // tengo este nuevo peso
        // ustedes cuanto peso tienen?
        do{
            continuarBalanceo = false;
            int miPeso = cnt.peso();
            Mensaje respuesta = null;
            boolean terminar = false;
            for( Connection cliente: this.clientes )
            {

                // esta funcion seria para enviar un mensaje y esperar su respuesta
                Mensaje m = createMensaje(
                        Mensaje.weight,
                        "oiga su peso" // el contenido no importa
                    );
                cliente.send(
                    m
                );
                while( this.respuestas.getOrDefault(m.getId(), null) == null ){
                    try
                    {
                        TimeUnit.SECONDS.sleep(2);
                    }
                    catch(InterruptedException ie)
                    {
                        ie.printStackTrace();
                    }
                }

                respuesta = this.respuestas.get(m.getId());
                respuestasEliminar(respuesta);

                // que no este vacio y que el contenido sea int
                if ( respuesta != null && respuesta.getContenido().getClass() == Integer.class )
                {
                    int peso = (int) respuesta.getContenido();
                    terminar |= balancearCliente(cliente, miPeso, peso);
                }

                continuarBalanceo |= terminar;

                if (terminar) break;
            }
        }while(continuarBalanceo);
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

    public void sendAware( String receptor, Mensaje mensaje )
    {
        boolean entregado = false;
        do{

            if( !cnt.local(receptor, mensaje) )
            {
                LOGGER = Utils.getLogger(this, "enviando " + mensaje.getContenido() + " a otro computador" );
                for( Connection c: this.clientes )
                {
                    entregado |= enviar( c, mensaje );
                }
            }

        } while( !entregado );
    }

    // envia a una conexion especifica
    public void send(Connection c, Mensaje data)
    {
        enviar(c, data);
    }

    private boolean enviar(Connection c, Mensaje data)
    {
        boolean exitoso = true;
        if (data.isRequest())
        {
            int intentos = 5;
            boolean seguir = true;
            while(seguir)
            {
                if ( // respuesta no este vacia, si se tenga al cliente y aun queden intentos
                        this.respuestas.getOrDefault(data.getId(), null) != null
                        && this.clientes.contains(c)
                        && intentos >= 0
                   )
                {
                    seguir = false;
                }
                else
                {
                    c.send(data);
                    intentos --;
                    try
                    {
                        TimeUnit.SECONDS.sleep(tiempo_espera);
                    }
                    catch(InterruptedException ie)
                    {
                        ie.printStackTrace();
                    }
                }
            }

            if ( this.respuestas.getOrDefault(data.getId(), null) != null )
            {
                Mensaje msg = this.respuestas.get(data.getId());

                if (msg.isRequest())
                {
                    cnt.respond(c, msg);
                }

                switch(msg.getSubType())
                {
                    case 1: // agregado
                        exitoso = true;
                        break;
                    case 2:
                        exitoso = false;
                        break;
                }

                // si es info, va a ser usada en algo, entonces no borrarla
                if (msg.isRespond() && msg.getSubType() != Mensaje.info )
                {
                    respuestasEliminar(msg);
                }
            }

            // agregar mensajes a los que se les espera request
            // a una lista, para intentar reenviarlos
            // casi mas bien, se podria lanzar un hilo, y
            // cuando llegue la respuesta, se le pasa a Conector,
            // para que este responda
            // y no toca estar revisando si ya contestaron un mensaje
            // en especifico
        }
        else
        {
            c.send(data);
        }
        return exitoso;
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

        if(
                ! this.esperas.getOrDefault(data.getId(), false)
                && this.respuestas.getOrDefault(data.getId(), null) == null
        )
        {
            this.esperas.put(data.getId(), true ); // ahora esta esperando
            // ignorar todos los demas mensajes de este mismo id

            Utils.print("mensajitico aceptado : " + data.toString() );

            if ( data.isRespond() )
            {
                Utils.print(" llega respuesta : " + data.toString() );
                respuestasAgregar(
                    data.getId(),
                    data
                );
            }
            cnt.respond(c, data);
        }
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
