package virus;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.Collectors;

import org.json.*;

public class Init{

    private Computador comp;

    public static void main(String args[]) {
        if (args.length > 0)
        {
            String configFile = args[0] ;
            Utils.print("iniciando con archivo de configuracion : " + configFile );
            new Init(configFile);
        }
        else
        {
            Utils.print("por favor espeficiar archivo de configuracion");
        }
    }

    private Init( String config )
    {
        cargarConfiguracion( config );
    }

    public void cargarConfiguracion( String nombre_archivo )
    {
        JSONObject obj = new JSONObject( readFile(nombre_archivo) );
        this.comp = new Computador(
            obj.getInt("puerto"),
            obj.getInt("umbral")
        );

        obj.getJSONArray("paises").forEach( o -> {
            Utils.print(o.getClass());
            jsonAPais( (JSONObject) o);
        });

        comp.imprimir();
        obj.getJSONArray("conexiones").forEach( ob -> {
            this.comp.agregarConexion(
                ( (JSONObject) ob ).getString("dir"),
                ( (JSONObject) ob ).getInt("port")
            );
        });

        int pasos = obj.getInt("pasos");
        this.comp.step(pasos);
        Utils.print("se leen " + pasos + " pasos");
    }

    public void jsonAPais( JSONObject jo )
    {
        Utils.print("leyendo nuevo pais ");
        comp.agregarPais(
            jo.getString( "nombre" ),
            Integer.parseInt( jo.getString( "poblacion" ) ),
            Integer.parseInt( jo.getString( "enfermos" ) ),
            Double.parseDouble( jo.getString( "alta_vulnerabilidad" ) ),
            Double.parseDouble( jo.getString( "aislamiento" ) ),
            Double.parseDouble( jo.getString( "posibilidad_viaje" ) ),
            Double.parseDouble( jo.getString( "posibilidad_viaje_aereo" ) ),
            jo.getJSONArray( "vecinos" ).toList().toArray(new String[0]),
            jo.getJSONArray( "vecinos_aereos" ).toList().toArray(new String[0])
        );
    }

    // cargar un archivo
    // tomado de : https://www.thepolyglotdeveloper.com/2015/03/parse-json-file-java/
    public static String readFile(String filename) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}

