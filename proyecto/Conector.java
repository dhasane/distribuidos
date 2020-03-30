import java.util.List;

public abstract class Conector {

    // responder a un mensaje recibido
    abstract public void respond(Connection c, Mensaje respuesta);

    // evento de desconexion de un socket
    abstract public void disconnect(Connection c);

    // en caso de llegar una nueva conexion
    abstract public void nuevaConexion(Connection c);

    // la cantidad de computo que realiza este conector
    abstract public int peso();

    // retorna el peso de los objetos
    abstract public List<Integer> pesoObjetos();

    // retorna el objeto en la posicion del index
    abstract public Object getObject(int index);

    // verificar si el receptor esta en local, y si lo esta pasarle el mensaje
    abstract public boolean local(String receptor, Mensaje mensaje);
}
