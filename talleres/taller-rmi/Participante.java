import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public abstract class Participante implements Remote, Serializable {

	private static final long serialVersionUID = 1L;

	private int numero;
    Participante(final int val_inicial) {
        numero = val_inicial;
    }
	public synchronized void sumar(){
        numero++;
    }
    public synchronized void restar(){
        numero--;
    }
    public int getNumero(){
        return numero;
    }

    abstract int call() throws RemoteException;
    abstract int add( int a, int b) throws RemoteException;

    public static void main(final String args[]) {
        try {

            // ya que deben ser 3 diferentes
            // aqui para ya ponerlos de manera distribuida, solo
            // seria meterlos en mains distintos :v
            final Participante p1 = new Participante(70){
                int call()
                {
                    this.sumar();
                    return this.getNumero();
                }
                int add(final int a, final int b) { return 0; }
            };

            final Participante p2 = new Participante(27){
                int call()
                {
                    this.restar();
                    return this.getNumero();
                }
                int add(final int a, final int b) { return 0; }
            };

            final Participante p3 = new Participante(0){
                int call() { return 0; }
                int add( final int v1, final int v2 )
                {
                    return v1 + v2;
                }
            };

            final Registry registry = LocateRegistry.getRegistry(null);
            registry.bind("+1", p1);
            registry.bind("-1", p2);
            registry.bind("add", p3);

        } catch ( final Exception e ) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
