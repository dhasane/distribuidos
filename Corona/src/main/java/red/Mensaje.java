package red;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import virus.Utils;

public class Mensaje implements Serializable{

    private static final long serialVersionUID = 129329939L;

    // public static int simple  = 0; // no espera respuesta
    public static int saludo  = 0; // no espera respuesta

    public static int request = 1; // espera respuesta
    public static int respond = 2; // respuesta de un request
    public static int accept  = 3; // respuesta de una respuesta :v

    // request
    public static double add     = 1.1; // agregar el elemento enviado
    public static double weight  = 1.2; // pide el "peso de procesamiento"
    public static double step    = 1.3; // tipo de request, envia un paso de tiempo
    public static double viajero = 1.4; // es mas facil declararlas por aca ....

    // reply
    public static double agregado = 2.1;
    public static double noAgregado = 2.2;

    private double tipo;
    private Object contenido;

    private String id;

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;


    public Mensaje(double tipo, Object contenido)
    {
        // usar la fecha como id
        this.id = LocalDateTime.now().format(formatter);

        this.tipo = tipo;
        this.contenido = contenido;
    }

    public Mensaje(double tipo, String id, Object contenido)
    {
        this.id = id;
        this.tipo = tipo;
        this.contenido = contenido;
    }

    public String getId()
    {
        return this.id;
    }

    public int getBaseType()
    {
        return (int) this.tipo;
    }

    public int getSubType()
    {
        int y = (int) (this.tipo * 10) % 10;
        return y;
    }

    public boolean isSaludo()
    {
        boolean ans = ((int) this.tipo) == saludo;
        // Utils.print("mensaje es " + this.tipo + " <-> " + saludo + " => " + ( ans? "1":"0") );
        return ans;
    }

    public boolean isRequest()
    {
        boolean ans = ((int) this.tipo) == request;
        // Utils.print("mensaje es " + this.tipo + " <-> " + request + " => " + ( ans? "1":"0") );
        return ans;
    }

    public boolean isRespond()
    {
        boolean ans = ((int) this.tipo) == respond;
        // Utils.print("mensaje es " + this.tipo + " <-> " + respond + " => " + ( ans? "1":"0") );
        return ans;
    }

    public Object getContenido()
    {
        return this.contenido;
    }

    public double getTipo()
    {
        return this.tipo;
    }

    public String toString()
    {
        return this.tipo + ":" + this.contenido + ":" + this.id;
    }
}
