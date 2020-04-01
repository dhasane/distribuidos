import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class Test{
    public static void main(String args[]) {
        test1();
    }

    public static void crearPais()
    {

    }

    // comprobar la interaccion entre paises dentro de un solo computador
    public static void test2()
    {
        int c1 = 4321;
        Computador comp = new Computador(c1, 1000);

        String local = "127.0.0.1";

        String[] vecinos = {"peru"};
        String[] vecinos_aereos = {"chile"};

        comp.agregarPais(
            "colombia", // nombre
            2000, // poblacion
            3, // enfermos iniciales
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5, // posibilidad viaje tierra
            0.2, // posibilidad viaje aire
            vecinos,
            vecinos_aereos
        );

        String[] vecinos1 = {"colombia"};
        String[] vecinos_aereos1 = {"chile"};

        comp.agregarPais(
            "peru",
            5000,
            0,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos1,
            vecinos_aereos1
        );

        String[] vecinos2 = {};
        String[] vecinos_aereos2 = {"peru","colombia"};
        comp.agregarPais(
            "chile",
            3000,
            0,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos2,
            vecinos_aereos2
        );

        comp.step(5);

        try
        {
            TimeUnit.SECONDS.sleep(15);
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        comp.detener();
    }

    // comprobar la interaccion entre varios computadores
    public static void test1()
    {
        int c1 = 4321;
        int c2 = 5432;
        int c3 = 8765;
        Computador comp1 = new Computador(c1, 1000);
        Computador comp2 = new Computador(c2, 1000);
        Computador comp3 = new Computador(c3, 1000);


        String local = "127.0.0.1";

        String[] vecinos = {"peru"};
        String[] vecinos_aereos = {"chile"};

        comp1.agregarPais(
            "colombia",
            2000,
            3,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos,
            vecinos_aereos
        );
        comp1.agregarPais(
            "italia",
            1000,
            3,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos,
            vecinos_aereos
        );

        String[] vecinos1 = {"colombia"};
        String[] vecinos_aereos1 = {"chile"};

        comp2.agregarPais(
            "peru",
            5000,
            0,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos1,
            vecinos_aereos1
        );

        String[] vecinos2 = {};
        String[] vecinos_aereos2 = {"peru","colombia"};
        comp2.agregarPais(
            "chile",
            3000,
            0,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos2,
            vecinos_aereos2
        );
        comp2.agregarPais(
            "china",
            4000,
            0,
            0.3, // alta_vulnerabilidad
            0.7, // aislamiento
            0.5,
            0.2,
            vecinos2,
            vecinos_aereos2
        );

        // comp1.step(20); // esto lo voy a probar despues, que va a tocar cambiar unas cosas
        comp1.agregarConexion(local, c2);
        comp1.agregarConexion(local, c3);
        comp3.agregarConexion(local, c2);
        comp1.step(20);

        try
        {
            TimeUnit.SECONDS.sleep(50);
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        comp1.imprimir();
        comp2.imprimir();
        comp3.imprimir();

        // comp1.detener();
        // comp2.detener();
        // comp3.detener();


        // aqui deberian quedar
        // comp1 : total 5000
        // comp2 : total 5000
    }
}
