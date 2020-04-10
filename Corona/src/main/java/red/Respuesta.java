package red;

public class Respuesta{

    private boolean respondido;
    private Mensaje mensaje;
    public Respuesta()
    {
        this.respondido = false;
        this.mensaje = null;
    }

    public void agregarRespuesta(Mensaje m)
    {
        this.respondido = true;
        this.mensaje = m;
    }

    public boolean estado()
    {
        return this.respondido;
    }

    public Mensaje conseguirMensaje()
    {
        return this.mensaje;
    }
}
