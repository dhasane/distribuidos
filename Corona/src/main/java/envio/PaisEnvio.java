package envio;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import virus.*;

// aqui se va a tener la informacion de un pais
public class PaisEnvio implements Serializable{

    private static final long serialVersionUID = 129329749L;

    private String nombre;
    private int poblacion;
    private int enfermos;
    private boolean continuar;

    private Map<String, String[]> vecinos;

    private double alta_vulnerabilidad;
    private double aislamiento;

    public PaisEnvio( Pais p )
    {
        this.nombre = p.getNombre();
        this.poblacion = p.getPoblacion();
        this.enfermos = p.getEnfermos();
        this.alta_vulnerabilidad = p.getAltaVulnerabilidad();
        this.aislamiento = p.getAislamiento();
        this.vecinos = p.getVecinos();
    }

    public PaisEnvio(
        String nombre,
        int poblacion,
        int enfermos,
        double vulnerabilidad,
        double aislamiento,
        Map<String, String[]> vecinos
    )
    {
        this.nombre = nombre;
        this.poblacion = poblacion;
        this.enfermos = enfermos;
        this.alta_vulnerabilidad = vulnerabilidad;
        this.aislamiento = aislamiento;
        this.vecinos = vecinos;
    }

    public Map<String, String[]> getVecinos()
    {
        return this.vecinos;
    }

    public int getEnfermos()
    {
        return this.enfermos;
    }

    public int getPoblacion()
    {
        return this.poblacion;
    }

    public String getNombre()
    {
        return this.nombre;
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
