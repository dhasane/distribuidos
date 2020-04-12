package envio;
import java.io.Serializable;

public class EstadoPais implements Serializable{

    private static final long serialVersionUID = 129381749L;

    private String nombre;
    private int poblacion;
    private int enfermos;
    private String[] direccion;

    public EstadoPais( String nombre, int poblacion, int enfermos, String[] direccion )
    {
        this.nombre = nombre;
        this.poblacion = poblacion;
        this.enfermos = enfermos;
        this.direccion = direccion;
    }

    public String[] getDireccion()
    {
        return this.direccion;
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
}
