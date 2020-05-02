
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Accion extends Remote{
    Integer act( int val ) throws RemoteException;
}
