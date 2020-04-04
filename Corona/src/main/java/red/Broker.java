package red;
import java.util.List;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
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

    public Broker(Conector cnt, int serverPort, int umbral)
    {
        try
        {
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
                respuesta = cliente.sendRespond(
                        new Mensaje(
                            Mensaje.weight,
                            "oiga su peso" // el contenido no importa
                        )
                );

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
                    new Mensaje(
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
        if( !cnt.local(receptor, mensaje) )
        {
            LOGGER = Utils.getLogger(this, "enviando " + mensaje.getContenido() + " a otro computador" );
            this.clientes.forEach( x -> {
                enviar( x, mensaje );
            });
        }
    }

    // envia a una conexion especifica
    public void send(Connection c, Mensaje data)
    {
        enviar(c, data);
    }

    private void enviar(Connection c, Mensaje data)
    {
        if (data.isRequest())
        {
            // agregar mensajes a los que se les espera request
            // a una lista, para intentar reenviarlos
            // casi mas bien, se podria lanzar un hilo, y
            // cuando llegue la respuesta, se le pasa a Conector,
            // para que este responda
            // y no toca estar revisando si ya contestaron un mensaje
            // en especifico
        }
        c.send(data);
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

    public void respond(Connection c, Mensaje data)
    {
        // por el momento es igual, pero esto me permite buscar reply
        // if ( data.isRespond() )
        // {
        //     Utils.print(" llega respuesta : " + data.toString() );
        //     this.respuestas.put(
        //         data.getId(),
        //         data.getContenido()
        //     );
        // }
        // else
        {
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
