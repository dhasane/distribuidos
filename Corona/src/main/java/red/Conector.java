package red;
import java.util.List;

public abstract class Conector {

    // responder a un mensaje recibido
    abstract public void respond(Connection c, Mensaje respuesta);

    // en caso de llegar una nueva conexion
    abstract public void nuevaConexion(Connection c);

    // enviar un primer mensaje a una nueva conexion
    abstract public void mensajeSaludo(Connection c);
}
