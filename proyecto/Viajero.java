import java.io.Serializable;

class Viajero implements Serializable{

    private boolean enfermo;
    private String origen;
    private String destino;

    Viajero(boolean enfermo, String origen, String destino)
    {
        this.enfermo = enfermo;
        this.origen = origen;
        this.destino = destino;
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
