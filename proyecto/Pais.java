import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// aqui se va a tener la informacion de un pais

class Pais extends Thread implements Serializable{

    private String nombre;
    private int poblacion;
    private int enfermos;
    private boolean continuar;
    private int steps; // pasos que faltan para ir al tiempo

    private Broker broker;
    private double posibilidad_viaje;
    private double posibilidad_viaje_aereo;

    private String[] vecinos;
    private String[] vecinos_aereos;

    private int tiempo_descanso = 1;

    Pais(
            Broker broker,
            String nombre,
            int poblacion,
            int enfermos,
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
        this.posibilidad_viaje = posibilidad_viaje;
        this.posibilidad_viaje_aereo = posibilidad_viaje_aereo;
        this.vecinos = vecinos;
        this.vecinos_aereos = vecinos_aereos;
        ini(0);
    }

    Pais( PaisEnvio p, Broker broker )
    {
        this.broker = broker;
        this.nombre = p.getNombre();
        this.poblacion = p.getPoblacion();
        this.enfermos = p.getEnfermos();
        this.posibilidad_viaje = p.getPosibilidad_viaje();
        this.posibilidad_viaje_aereo = p.getPosibilidad_viaje_aereo();
        this.vecinos = p.getVecinos();
        this.vecinos_aereos = p.getVecinos_aereos();
        ini(p.getSteps());
    }

    Pais( Pais p, Broker broker )
    {
        this.broker = broker;
        this.nombre = p.nombre;
        this.poblacion = p.poblacion;
        this.enfermos = p.enfermos;
        this.posibilidad_viaje = p.posibilidad_viaje;
        this.posibilidad_viaje_aereo = p.posibilidad_viaje_aereo;
        this.vecinos = p.vecinos;
        this.vecinos_aereos = p.vecinos_aereos;
        ini(0);
    }

    private void ini(int steps)
    {
        this.steps = steps;
        this.continuar = true;
        this.start();
    }

    public String prt()
    {
        return this.nombre + " : (" + this.enfermos + "/" + this.poblacion + ")";
    }

    public void run()
    {
        while(continuar)
        {
            if(steps > 0)
            {
                Utils.print("pasa un dia : " + prt() + " quedan " + steps + " dias" );
                infectar();
                if( random(0,1) < posibilidad_viaje )
                {
                    viaje(this.vecinos);
                }
                if( random(0,1) < posibilidad_viaje_aereo )
                {
                    viaje(this.vecinos_aereos);
                }
                this.steps--;
            }

            try
            {
                TimeUnit.SECONDS.sleep(this.tiempo_descanso);
            }
            catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }

        Utils.print(this.nombre + " detenido");
    }


    public void step(int pasos)
    {
        this.steps += pasos ;
    }

    // detiene la ejecucion del thread
    public void detener()
    {
        // tal vez seria mejor usar Thread.stop(), aunque no se si eso
        // tenga algun problema
        this.continuar = false;
    }

    // probablemente no sea necesario tener un Thread continue,
    // para eso ya esta la creacion de pais y start

    // da un paso de tiempo
    public void infectar(){
        // despues puedo poner una formula mas interesante
        int nuevos_enfermos = this.enfermos*this.enfermos;
        this.enfermos = nuevos_enfermos < this.poblacion ? nuevos_enfermos : this.poblacion ;
    }

    public void viajeroEntrante(Viajero v)
    {
        agregarPoblacion();
        if(v.enfermo())
        {
            agregarEnfermos();
        }
    }

    private void viaje(String[] destinos)
    {
        // viaje aleatorio a uno de los paises destino

        if ( destinos.length == 0 )
            return;
        String pais = destinos[ (int) random(0, destinos.length) ];
        Utils.print("se mueve de : " + this.getNombre() + " a: " + pais);

        this.broker.sendAware(
            pais,
            new Mensaje(
                Mensaje.simple,
                new Viajero(
                    this.enfermos > 0 ? random(0,1) < this.poblacion / this.enfermos : false
                )
            )
        );
        reducirPoblacion();
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
        return this.nombre + " : " + this.poblacion;
    }

    private synchronized void reducirEnfermos()
    {
        this.enfermos--;
    }

    private synchronized void agregarEnfermos()
    {
        this.enfermos++;
    }

    private synchronized void reducirPoblacion()
    {
        this.poblacion--;
    }

    private synchronized void agregarPoblacion()
    {
        this.poblacion++;
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
}
