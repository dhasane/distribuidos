package virus;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.*;

public class Init{

    private Computador comp;

    public static void main(String args[]) {
        String configFile = args.length > 0 ? args[0] : "paises.json" ;
        Utils.print("iniciando con archivo de configuracion : " + configFile );
        new Init(configFile);
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

        JSONArray ja = obj.getJSONArray("paises");

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            JSONObject jo = ja.getJSONObject(a);
            // Utils.print("leyendo nuevo pais " + jo );
            comp.agregarPais(
                jo.getString( "nombre" ),
                Integer.parseInt( jo.getString( "poblacion" ) ),
                Integer.parseInt( jo.getString( "enfermos" ) ),
                Double.parseDouble( jo.getString( "alta_vulnerabilidad" ) ),
                Double.parseDouble( jo.getString( "aislamiento" ) ),
                aVecinos(jo.getJSONArray( "vecinos" )),
                aVecinos(jo.getJSONArray( "vecinos_aereos" )),
                jo.getInt( "port" )
            );
        }

        ja = obj.getJSONArray("conexiones");

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            JSONObject jo = ja.getJSONObject(a);
            this.comp.agregarConexion(
                jo.getString("dir"),
                jo.getInt("port")
            );
        }
        comp.imprimir();
    }

    public List<String[]> aVecinos(JSONArray ja)
    {
        List<String[]> vecinos = new ArrayList<String[]>();

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            JSONObject jo = ja.getJSONObject(a);
            String[] val = new String[2];

            val[0] = jo.getString( "dir" );
            val[1] = jo.getString( "port" );

            vecinos.add(val);
        }

        return vecinos;
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

