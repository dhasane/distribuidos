package red;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import virus.Utils;

public class Mensaje implements Serializable{

    private static final long serialVersionUID = 129329939L;

    // public static int simple  = 0; // no espera respuesta
    public static int saludo  = 0; // primer mesanje al ser conectado

    public static int request = 1; // espera respuesta
    public static int respond = 2; // respuesta de un request
    public static int accept  = 3; // respuesta de una respuesta :v

    // request
    public static double add     = 1.1; // agregar el elemento enviado
    public static double weight  = 1.2; // pide el "peso de procesamiento"
    public static double estado  = 1.3; // envia el estado

    // reply
    public static double agregado   = 2.1;
    public static double noAgregado = 2.2;
    public static double info       = 2.3;

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
        return ((int) this.tipo) == saludo;
    }

    public boolean isRequest()
    {
        return ((int) this.tipo) == request;
    }

    public boolean isRespond()
    {
        return ((int) this.tipo) == respond;
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
