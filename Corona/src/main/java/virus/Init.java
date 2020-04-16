package virus;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.*;

public class Init{

    private Computador comp;
    private Map<String, Map<String, String> > conexiones;
    private Map<String, Map<String, String> > paises;

    public static void main(String args[]) {
        if (args.length > 1)
        {
            String config = args[0];
            String nombre = args[1];
            Utils.print("iniciando con archivo de configuracion : " + config + " con nombre : " + nombre);
            new Init(config, nombre);
        }
        else
        {
            Utils.print("por favor espeficiar archivo de configuracion y nombre");
        }
    }

    private void getGeneral(JSONObject json)
    {
        JSONArray jac = json.getJSONArray("conexiones");
        for( int a = 0 ; a < jac.length() ; a++ )
        {
            JSONObject jo = jac.getJSONObject(a);
            Map<String, String> value = new HashMap<String, String>();
            value.put( "dir", jo.getString("dir"));
            value.put( "port", jo.getString("port"));
            this.conexiones.put(
                jo.getString("nombre"),
                value
            );
        }

        JSONArray jap = json.getJSONArray("paises");

        for( int a = 0 ; a < jap.length() ; a++ )
        {
            JSONObject jo = jap.getJSONObject(a);
            Map<String, String> value = new HashMap<String, String>();
            // ip
            value.put( "en", jo.getString("en") );
            value.put( "dir", this.conexiones.get( jo.getString("en") ).get("dir") ) ;
            value.put( "port", jo.getString("port"));
            this.paises.put(
                jo.getString("nombre"),
                value
            );
        }

        // Utils.print(this.conexiones);
        // Utils.print(this.paises);
    }

    private Init( String config, String nombre )
    {
        this.conexiones = new HashMap<String, Map<String, String> >();
        this.paises = new HashMap<String, Map<String, String> >();

        JSONObject obj = new JSONObject( readFile(config) );
        getGeneral(obj);

        // Utils.print(Integer.parseInt(this.conexiones.get(nombre).get("port")));

        String thisPort = this.conexiones.get(nombre).get("port");
        this.comp = new Computador(
            Integer.parseInt(this.conexiones.get(nombre).get("port"))
        );

        JSONArray ja = obj.getJSONArray("info");

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            JSONObject jo = ja.getJSONObject(a);
            // Utils.print("leyendo nuevo pais " + jo );
            String nombreP = jo.getString("nombre");
            // verificar si el pais se encuentra en el computador actual
            if ( nombre.equals( this.paises.get(nombreP).get("en") ) )
            {
                // Utils.print("agregando " + nombreP );
                comp.agregarPais(
                    nombreP,
                    Integer.parseInt( jo.getString( "poblacion" ) ),
                    Integer.parseInt( jo.getString( "enfermos" ) ),
                    Double.parseDouble( jo.getString( "alta_vulnerabilidad" ) ),
                    Double.parseDouble( jo.getString( "aislamiento" ) ),
                    aVecinos(jo.getJSONArray( "vecinos" )),
                    Integer.parseInt( this.paises.get(nombreP).get("port") )
                );
            }
        }

        ja = obj.getJSONArray("conexiones");

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            JSONObject jo = ja.getJSONObject(a);
            if( jo.getString("port") != thisPort)
            {
                this.comp.agregarConexion(
                    jo.getString("dir"),
                    Integer.parseInt( jo.getString("port") )
                );
            }
        }
        comp.imprimir();
    }

    public List<String[]> aVecinos(JSONArray ja)
    {
        List<String[]> vecinos = new ArrayList<String[]>();

        for( int a = 0 ; a < ja.length() ; a++ )
        {
            String nomP = (String) ja.get(a);
            String[] val = new String[3];

            // Utils.print( nomP + " => " + this.paises.get(nomP).get("dir") + ":" + this.paises.get(nomP).get("port"));

            val[0] = nomP;
            val[1] = this.paises.get(nomP).get("dir");
            val[2] = this.paises.get(nomP).get("port");

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

