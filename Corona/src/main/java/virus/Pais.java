package virus;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import red.Mensaje;
import red.Conexiones;
import red.Connection;
import red.Conector;
import envio.PaisEnvio;
// aqui se va a tener la informacion de un pais

public class Pais extends Conector{
    private Logger LOGGER;
    private String nombre;
    private int poblacion;
    private int enfermos;
    private boolean continuar;

    private int tiempo_descanso = 1;

    private double alta_vulnerabilidad;
    private double aislamiento;

    private Conexiones con;

    public Pais(
            String nombre,
            int poblacion,
            int enfermos,
            double alta_vulnerabilidad,
            double aislamiento,
            List<String[]> vecinos,
            List<String[]> vecinosAereos,
            int serverPort
        )
    {
        this.nombre = nombre;
        this.poblacion = poblacion;
        this.enfermos = enfermos;
        this.alta_vulnerabilidad = alta_vulnerabilidad;
        this.aislamiento = aislamiento;

        this.con = new Conexiones(this, serverPort);
        vecinos.forEach( v -> {
            if ( v[0].length() > 0 && v[1].length() > 0 )
                this.con.agregar(v[0], Integer.parseInt(v[1]));
        });

        this.continuar = true;
        LOGGER = Utils.getLogger(this, this.nombre);
        this.start();
    }

    public Pais( PaisEnvio p )
    {
        this.nombre = p.getNombre();
        this.poblacion = p.getPoblacion();
        this.enfermos = p.getEnfermos();
        this.alta_vulnerabilidad = p.getAltaVulnerabilidad();
        this.aislamiento = p.getAislamiento();

        // abre un puerto de servidor en un puerto cualquiera
        this.con = new Conexiones(this);
        p.getVecinos().forEach( v -> {
            if ( v[0].length() > 0 && v[1].length() > 0 )
                this.con.agregar(v[0], Integer.parseInt(v[1]));
        });

        this.continuar = true;
        LOGGER = Utils.getLogger(this, this.nombre);

        this.start();

    }

    public List<String[]> getVecinos()
    {
        return this.con.getConexiones();
    }

    public String prt()
    {
        return this.nombre + " : (" + this.enfermos + "/" + this.poblacion + ")";
    }

    public void run()
    {
        LOGGER.log( Level.INFO, "iniciando : " + this.nombre );
        while(continuar)
        {
            try{
                infectar();
                this.con.send(
                    new Mensaje(
                        Mensaje.estado,
                        new PaisEnvio(this)
                    )
                );
                LOGGER.log(Level.INFO, "pasa un dia : " + prt() );
                Utils.print( "pasa un dia : " + prt() );
                Thread.sleep(this.poblacion);
            }
            catch(InterruptedException ie)
            {
                continuar = false;
            }
        }
        LOGGER.log(Level.INFO, "detenido" );
    }

    public void detener()
    {
        this.continuar = false;
    }

    // da un paso de tiempo
    public synchronized void infectar(){
        // intentar simular una tasa de infeccion de 1.6
        // int nuevos_enfermos = this.enfermos + (int) (this.enfermos * 1.6);

        double nuevos_enfermos = (this.enfermos * 1.6);
        nuevos_enfermos += nuevos_enfermos * this.alta_vulnerabilidad;
        nuevos_enfermos -= nuevos_enfermos * this.aislamiento;

        if (nuevos_enfermos < 0 ) nuevos_enfermos = 0;

        int nuevos = this.enfermos + ((int)nuevos_enfermos);

        this.enfermos = nuevos < this.poblacion ? nuevos : this.poblacion ;
    }

    // da un paso de tiempo
    public synchronized void infectar(double posibilidadInfecion, String nombre){
        // intentar simular una tasa de infeccion de 1.6
        // int nuevos_enfermos = this.enfermos + (int) (this.enfermos * 1.6);

        // Utils.print( "posibilidad " +  posibilidadInfecion);

        // solo agregar dos, por el momento
        double nuevos_enfermos = random(0, 1) < posibilidadInfecion ? 2 : 0;
        // Utils.print("nuevos infectados : " + this.enfermos + " " + nuevos_enfermos);

        nuevos_enfermos += nuevos_enfermos * this.alta_vulnerabilidad;
        nuevos_enfermos -= nuevos_enfermos * this.aislamiento;

        if (nuevos_enfermos < 0 ) nuevos_enfermos = 0;

        int nuevos = this.enfermos + ((int)nuevos_enfermos);

        if (nuevos != this.enfermos && nuevos < this.poblacion )
        {
            Utils.print("alguien se infecta en " + this.nombre + " por " + nombre);
        }

        this.enfermos = nuevos < this.poblacion ? nuevos : this.poblacion ;
    }

    public int getPoblacion()
    {
        return this.poblacion;
    }

    public String getNombre()
    {
        return this.nombre;
    }

    public String toString()
    {
        return this.nombre + " : " + this.enfermos + " / " + this.poblacion;
    }

    private synchronized void reducirEnfermos()
    {
        this.enfermos--;
    }

    private synchronized void agregarEnfermos()
    {
        this.enfermos++;
    }

    private synchronized void reducirPoblacion(boolean enfermo)
    {
        this.poblacion--;
        if(enfermo)
        {
            reducirEnfermos();
        }
    }

    private synchronized void agregarPoblacion(boolean enfermo)
    {
        this.poblacion++;
        if(enfermo)
        {
            agregarEnfermos();
        }
    }

    private double random( int inferior, int superior )
    {
        double val = Math.random();
        if ( val < 0 ) val *= -1;
        val *= (superior-inferior);
        return val;
    }

    public int getEnfermos()
    {
        return this.enfermos;
    }

    public double getAislamiento()
    {
        return this.aislamiento;
    }

    public double getAltaVulnerabilidad()
    {
        return this.alta_vulnerabilidad;
    }


    @Override
    public void respond(Connection c, Mensaje respuesta)
    {
        LOGGER.log( Level.INFO, "mensaje entrante: " + respuesta.toString() );
        // Utils.print(  "mensaje entrante: " + respuesta.toString() );

        if(respuesta.isRequest())
        {
            double tipo = Mensaje.noAgregado;
            Object contenido = null;

            // TODO hacer que esto sea con un objeto mas liviano que todo el estado del pais
            if ( respuesta.getTipo() == Mensaje.estado && respuesta.getContenido().getClass() == PaisEnvio.class)
            {
                PaisEnvio pe = (PaisEnvio) respuesta.getContenido();
                // Utils.print("en " + pe.getNombre() + " hay " + pe.getEnfermos() + "/" + pe.getPoblacion() );
                if  ( pe.getEnfermos() > 0 )
                {
                    // agregarEnfermos();
                    double porcentajeInfeccion = pe.getEnfermos();
                    porcentajeInfeccion /= pe.getPoblacion();

                    infectar(porcentajeInfeccion, pe.getNombre());
                }
                tipo = Mensaje.agregado;
            }

            // mensaje escuchado
            // importante siempre responder a un request
            this.con.send(
                c,
                new Mensaje(
                    tipo,
                    respuesta.getId(),
                    contenido
                )
            );
        }
        else if(respuesta.isRespond())
        {
            // esto aca no es realmente necesario, este tipo de mensaje es
            // mas para evitar reenviar mensajes

            // Utils.print("lega objetoooooooooo " + respuesta.toString());

            // en teoria aca se deberia enviar un accept
            // pero no los estoy manejando
        }

        // por el momento no se usan accept
    }

    @Override
    public void nuevaConexion(Connection c)
    {
        this.con.agregar(c);
    }
}
