
public abstract class Conector {

    // responder a un mensaje recibido
    abstract public void respond(Connection c, String respuesta);

    // evento de desconexion de un socket
    abstract public void disconnect(Connection c);

}
