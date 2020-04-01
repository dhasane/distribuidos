package envio;
import java.io.Serializable;

public class Viajero implements Serializable{

    private boolean enfermo;
    private String origen;
    private String destino;
    private String metodo;

    public Viajero(boolean enfermo, String origen, String destino, String metodo)
    {
        this.enfermo = enfermo;
        this.origen = origen;
        this.destino = destino;
        this.metodo = metodo;
    }

    public String prt()
    {
        return this.origen + " (" + this.metodo+ ") -> " + this.destino + (this.enfermo? " enfermo": " saludable") ;
    }

    public boolean enfermo()
    {
        return this.enfermo;
    }

    public String getOrigen()
    {
        return this.origen;
    }

    public String getDestino()
    {
        return this.destino;
    }
}
