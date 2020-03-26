import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Map;


import java.util.HashMap;

public class Broker{

    private ServerSocket listenSocket;
    private List<Connection> clientes;
    private Conector cnt;
    private int umbral; // umbral aceptable de diferencia entre pesos de distintos

    public Broker(Conector cnt, int serverPort, int umbral)
    {
        try
        {
            this.listenSocket = new ServerSocket(serverPort); //Inicializar socket con el puerto
            this.clientes = new ArrayList<Connection>();
            this.cnt = cnt;
            this.umbral = umbral;
            escucharConexionesEntrantes();
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

        // Utils.print("la diferencia es " + diferencia );
        if ( abs(diferencia) >= this.umbral )
        {
            // tu que tienes un peso suficientemente distinto al mio(dentro de un umbral), te paso una de mis clases que te acerque lo mejor posible al promedio entre tu peso y mi peso

            List<Integer> pesos = cnt.pesoObjetos();

            int index = -1;
            int paisMinimo = 0;
            int poblacion;

            int posicion = 0;
            for(Integer pais: pesos)
            {
                poblacion = diferencia - pais;

                // Utils.print( getNombre() + " cambio seria :" + diferencia + " -> " + poblacion + "   umbral : " + umbral);

                // -umbral < poblacion < umbral y que sea el minimo posible
                if( abs(poblacion) <= umbral && ( index == -1 || pais < paisMinimo) )
                {
                    paisMinimo = pais;
                    index = posicion;
                }
                posicion++;
            }

            // Utils.print("se selecciono el index : " + index);

            Object obj = cnt.getObject(index);

            // Utils.print( this.getNombre() + " el objeto que voy a enviar es :" + obj);
            // se le envia un "comando" y el objeto
            // algo asi como : "agregar", obj
            if (obj != null){
                cliente.send(
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
    private void escucharConexionesEntrantes()
    {
        // hilo que espera a que lleguen nuevos clientes
        new Thread( () -> {
            try{
                while(true) {
                    //Esperar en modo escucha al cliente
                    //Establecer conexion con el socket del cliente(Hostname, Puerto)

                    // Escucha nuevo cliente y agrega en lista
                    cnt.nuevaConexion(
                            new Connection( cnt, listenSocket.accept())
                    );
                }
            } catch(IOException e) {
                System.out.println("Listen socket:"+e.getMessage());
            }
        }).start();
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

    public synchronized boolean agregar(Connection c)
    {
        // no pueden haber repetidos
        if( !this.clientes.contains(c) )
        {
            this.clientes.add(c);
            return true;
        }
        return false;
    }

    // envia a una conexion especifica
    public void send(Connection c, String data)
    {
        c.send(
            new Mensaje(
                Mensaje.simple,
                data
            )
        );
    }

    public void send(Connection c, Mensaje data)
    {
        c.send(data);
    }

    // envia a todas las conexiones
    public void send(String data)
    {
        this.clientes.forEach( x -> {
            x.send(
                new Mensaje(
                    Mensaje.simple,
                    data
                )
            );
        });
    }
}
