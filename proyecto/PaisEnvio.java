
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// aqui se va a tener la informacion de un pais

class PaisEnvio implements Serializable{

    private String nombre;
    private int poblacion;
    private int enfermos;
    private boolean continuar;
    private int steps; // pasos que faltan para ir al tiempo

    private double posibilidad_viaje;
    private double posibilidad_viaje_aereo;

    private String[] vecinos;
    private String[] vecinos_aereos;

    PaisEnvio( Pais p )
    {
        this.nombre = p.getNombre();
        this.poblacion = p.getPoblacion();
        this.enfermos = p.getEnfermos();
        this.posibilidad_viaje = p.getPosibilidad_viaje();
        this.posibilidad_viaje_aereo = p.getPosibilidad_viaje_aereo();
        this.vecinos = p.getVecinos();
        this.vecinos_aereos = p.getVecinos_aereos();
        this.steps = p.getSteps();
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

    public int getPoblacion()
    {
        return this.poblacion;
    }

    public String getNombre()
    {
        return this.nombre;
    }
}
