import java.io.Serializable;

class Mensaje implements Serializable{

    public static int simple  = 0; // no espera respuesta
    public static int request = 1; // espera respuesta
    public static int respond = 2; // respuesta de un request
    public static int accept  = 3; // respuesta de una respuesta :v
    public static int add     = 4; // agregar el elemento enviado
    public static int weight  = 5; // pide el "peso de procesamiento"


    private int tipo;
    private Object contenido;

    public Mensaje(int tipo, Object contenido)
    {
        this.tipo = tipo;
        this.contenido = contenido;
    }

    Object getContenido()
    {
        return this.contenido;
    }

    int getTipo()
    {
        return this.tipo;
    }

    public String toString()
    {
        return this.tipo + ":" + this.contenido;
    }


}
