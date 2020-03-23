import java.io.Serializable;

// aqui se va a tener la informacion de un pais

class Pais implements Serializable{

    private String nombre;
    private int poblacion;
    private int enfermos;
    // private int

    // y mas cosas que estan faltando

    Pais(String nombre, int poblacion, int enfermos /* etc etc*/)
    {
        this.nombre = nombre;
        this.poblacion = poblacion;
        this.enfermos = enfermos;
    }

    public int getPoblacion()
    {
        return this.poblacion;
    }

    public String getNombre()
    {
        return this.nombre;
    }


    public String toString()
    {
        return this.nombre + " : " + this.poblacion;
    }


}
