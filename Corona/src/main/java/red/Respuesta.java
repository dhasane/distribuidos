package red;

public class Respuesta{

    private final String id;
    private final int tiempo_a_eliminar;

    Respuesta(final String id, final int t)
    {
        this.tiempo_a_eliminar = t;
        this.id = id;
    }

    public int getTiempo()
    {
        return this.tiempo_a_eliminar;
    }

    public String getId()
    {
        return this.id;
    }
}
