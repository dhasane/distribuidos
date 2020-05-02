
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// |             /> participante fuente 1
// coordinador   -> participante fuente 2
//               \> participante fuente 3

// leer fuente 1 -> + 1
// leer fuente 2 -> - 1
// leer funete 3 -> a + b

public class Coordinador {

    Coordinador()
    {
        try {
            // Getting the registry
            final Registry registry = LocateRegistry.getRegistry(null);

            // Looking up the registry for the remote object


            // 70 + 1 = 71
            // 27 - 1 = 26
            // 71 + 26 = 97

            final int value = ((Participante) registry.lookup("add")).add(
                            ((Participante) registry.lookup("+1")).call(),
                            ((Participante) registry.lookup("-1")).call()
                        );

            // Calling the remote method using the obtained object
            System.out.println(value);

            // System.out.println("Remote method invoked");
        } catch (final Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }

    public static void main(final String args[]) {
        new Coordinador();
    }

}
