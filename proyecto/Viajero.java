import java.io.Serializable;

class Viajero implements Serializable{

    private boolean enfermo;

    Viajero(boolean enfermo)
    {
        this.enfermo = enfermo;
    }

    public boolean enfermo()
    {
        return this.enfermo;
    }
}
