package red;

public class Respuesta{

    private String id;
    private int tiempo_a_eliminar;

    Respuesta(String id, int t)
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
