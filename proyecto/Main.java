import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class Main{
    public static void main(String args[]) {

        int c1 = 4321;
        int c2 = 5432;
        Computador comp1 = new Computador(c1, 1000);
        Computador comp2 = new Computador(c2, 1000);

        String local = "127.0.0.1";

        comp1.agregarPais(
            new Pais(
                "colombia",
                2000,
                3
            )
        );

        comp2.agregarPais(
            new Pais(
                "peru",
                5000,
                3
            )
        );

        comp2.agregarPais(
            new Pais(
                "chile",
                3000,
                3
            )
        );
        comp2.agregarPais(
            new Pais(
                "chile",
                3000,
                3
            )
        );
        comp2.agregarPais(
            new Pais(
                "chile",
                3000,
                3
            )
        );
        comp1.agregarConexion(local, c2);


        try
        {
            TimeUnit.SECONDS.sleep(15);
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        comp1.imprimir();
        comp2.imprimir();

        // aqui deberian quedar
        // comp1 : total 5000
        // comp2 : total 5000

    }
}
