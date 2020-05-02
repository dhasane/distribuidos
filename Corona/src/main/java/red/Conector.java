package red;

public abstract class Conector extends Thread{

    // responder a un mensaje recibido
    abstract public void respond(Connection c, Mensaje respuesta);

    // en caso de llegar una nueva conexion
    abstract public void nuevaConexion(Connection c);

    // retornar el nombre del conector
    abstract public String getNombre();
}
