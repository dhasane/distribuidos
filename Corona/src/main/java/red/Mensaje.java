package red;
import java.io.Serializable;

public class Mensaje implements Serializable{

    // public static int simple  = 0; // no espera respuesta
    public static int saludo  = 0; // no espera respuesta

    public static int request = 1; // espera respuesta
    public static int respond = 2; // respuesta de un request
    public static int accept  = 3; // respuesta de una respuesta :v

    // realmente quiero volver estos mensajes un subconjunto de request
    // podria hacerlos double... 1.1 -> add
    public static int add     = 4; // agregar el elemento enviado
    public static int weight  = 5; // pide el "peso de procesamiento"
    public static int step    = 6; // tipo de request, envia un paso de tiempo
    public static int viajero = 7; // es mas facil declararlas por aca ....

    public static String agregado = "valor agregado";

    private int tipo;
    private Object contenido;

    public Mensaje(int tipo, Object contenido)
    {
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public static boolean isRequest(int tipo)
    {
        // esto esta un asco, pero planeo ne cualquier caso
        // despues arreglarlo para no tener que hacer esto
        return tipo==4 || tipo==5|| tipo==6|| tipo==7;
    }

    public Object getContenido()
    {
        return this.contenido;
    }

    public int getTipo()
    {
        return this.tipo;
    }

    public String toString()
    {
        return this.tipo + ":" + this.contenido;
    }
}
