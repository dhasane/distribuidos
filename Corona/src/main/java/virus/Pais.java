package virus;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import red.Mensaje;
import red.Broker;
import envio.PaisEnvio;
import envio.Viajero;
// aqui se va a tener la informacion de un pais

public class Pais extends Thread implements Serializable{
    private Logger LOGGER;
    private String nombre;
    private int poblacion;
    private int enfermos;
    private boolean continuar;
    private int steps; // pasos que faltan para ir a tiempo

    // cuantas veces se han agregado pasos
    // funciona en cierta forma como un reloj, haciendo que
    // sea facilmente comparable contra el del computador
    private int maxStep;

    private Broker broker;
    private double posibilidad_viaje;
    private double posibilidad_viaje_aereo;

    private String[] vecinos;
    private String[] vecinos_aereos;

    private int tiempo_descanso = 1;
// Para cada país:
    // y el porcentaje de aislamiento de las personas.
    private double alta_vulnerabilidad;
    private double aislamiento;

    public Pais(
            Broker broker,
            String nombre,
            int poblacion,
            int enfermos,
            double alta_vulnerabilidad,
            double aislamiento,
            double posibilidad_viaje,
            double posibilidad_viaje_aereo,
            String[] vecinos,
            String[] vecinos_aereos
        )
    {
        this.broker = broker;
        this.nombre = nombre;
        this.poblacion = poblacion;
        this.enfermos = enfermos;
        this.alta_vulnerabilidad = alta_vulnerabilidad;
        this.aislamiento = aislamiento;
        this.posibilidad_viaje = posibilidad_viaje;
        this.posibilidad_viaje_aereo = posibilidad_viaje_aereo;
        this.vecinos = vecinos;
        this.vecinos_aereos = vecinos_aereos;
        this.maxStep = 0;
        this.steps = 0;
        this.continuar = true;
        LOGGER = Utils.getLogger(this, this.nombre);
        this.start();
    }

    public Pais( PaisEnvio p, Broker broker )
    {
        this.broker = broker;
        this.nombre = p.getNombre();
        this.poblacion = p.getPoblacion();
        this.enfermos = p.getEnfermos();
        this.alta_vulnerabilidad = p.getAltaVulnerabilidad();
        this.aislamiento = p.getAislamiento();
        this.posibilidad_viaje = p.getPosibilidad_viaje();
        this.posibilidad_viaje_aereo = p.getPosibilidad_viaje_aereo();
        this.vecinos = p.getVecinos();
        this.vecinos_aereos = p.getVecinos_aereos();
        this.maxStep = p.getMaxStep();
        this.steps = p.getSteps();
        this.continuar = true;
        LOGGER = Utils.getLogger(this, this.nombre);
        this.start();
    }

    public String prt()
    {
        return this.nombre + " : (" + this.enfermos + "/" + this.poblacion + ")";
    }

    public void run()
    {
        LOGGER.log( Level.INFO, "iniciando : " + this.nombre + " con " + this.steps + " pasos");
        while(continuar)
        {
            try{
                if(steps > 0)
                {
                    infectar();
                    if( random(0,1) < posibilidad_viaje )
                    {
                        viaje(this.vecinos, "tierra");
                    }
                    if( random(0,1) < posibilidad_viaje_aereo )
                    {
                        viaje(this.vecinos_aereos, "aire");
                    }
                    this.steps--;
                    LOGGER.log(Level.INFO, "pasa un dia : " + prt() + " quedan " + steps + " dias" );
                    Utils.print( "pasa un dia : " + prt() + " quedan " + steps + " dias" );
                }
                Thread.sleep(1000);
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

    public synchronized void step(int pasos)
    {
        LOGGER.log(Level.INFO, "agegando " + pasos + " pasos" );
        this.steps += pasos ;
        this.maxStep += pasos;
    }

    // da un paso de tiempo
    public synchronized void infectar(){
        // intentar simular una tasa de infeccion de 1.6
        // int nuevos_enfermos = this.enfermos + (int) (this.enfermos * 1.6);

        String modificacion = "";
        int nuevos_enfermos = (int) (this.enfermos * 1.6);
        modificacion += nuevos_enfermos + " :: ";
        nuevos_enfermos += nuevos_enfermos * this.alta_vulnerabilidad;
        modificacion += this.alta_vulnerabilidad + " ->" + nuevos_enfermos + " :: " + nuevos_enfermos + " - " + nuevos_enfermos*this.aislamiento + " -> ";
        nuevos_enfermos -= nuevos_enfermos * this.aislamiento;
        modificacion += nuevos_enfermos + " :: ";

        if (nuevos_enfermos < 0 ) nuevos_enfermos = 0;

        this.enfermos = nuevos_enfermos < this.poblacion ? this.enfermos + nuevos_enfermos : this.poblacion ;
        modificacion += this.enfermos + " :: ";
        LOGGER.log(Level.INFO, "infectados a " + modificacion );
    }

    public void viajeroEntrante(Viajero v)
    {
        agregarPoblacion(v.enfermo());
        LOGGER.log(Level.INFO,  "entra viajero : " + v.prt() + " |  pais : " + prt() );
        Utils.print( "entra viajero : " + v.prt() + " |  pais : " + prt() );
    }

    private void viaje(String[] destinos, String metodo)
    {
        // viaje aleatorio a uno de los paises destino

        if ( destinos.length == 0 )
            return;
        String pais = destinos[ (int) random(0, destinos.length) ];

        boolean enfermo = this.enfermos > 0 ? random(0,1) < this.enfermos / this.poblacion : false;

        Viajero v = new Viajero(
            enfermo,
            this.nombre,
            pais,
            metodo
        );
        LOGGER.log(Level.INFO, "nuevo viajero : " + v.prt() );

        this.broker.sendAware(
            pais,
            new Mensaje(
                Mensaje.viajero,
                v
            )
        );
        reducirPoblacion(enfermo);
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

    public double getPosibilidad_viaje()
    {
        return this.posibilidad_viaje;
    }

    public double getPosibilidad_viaje_aereo()
    {
        return this.posibilidad_viaje_aereo;
    }

    public String[] getVecinos()
    {
        return this.vecinos;
    }

    public String[] getVecinos_aereos()
    {
        return this.vecinos_aereos;
    }

    public int getSteps()
    {
        return this.steps;
    }

    public int getMaxStep()
    {
        return this.maxStep;
    }

    public double getAislamiento()
    {
        return this.aislamiento;
    }

    public double getAltaVulnerabilidad()
    {
        return this.alta_vulnerabilidad;
    }
}
