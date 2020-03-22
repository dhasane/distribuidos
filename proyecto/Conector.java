
public abstract class Conector {

    // responder a un mensaje recibido
    abstract public void respond(Connection c, Mensaje respuesta);

    // evento de desconexion de un socket
    abstract public void disconnect(Connection c);

    // la cantidad de computo que realiza este conector
    abstract public int peso();

    // retorna un objeto, para enviarselo a otro
    abstract public Object conseguirObjetoPeso(int diferencia,int umbral);
}
