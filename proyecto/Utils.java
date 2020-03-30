import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Utils{

    public static Logger getLogger(Object obj, String nombre)
    {
        Logger log = Logger.getLogger(obj.getClass().getName() + nombre);
        try{
            log.addHandler(new FileHandler( "logs/"  + obj.getClass().getName() + "-" + nombre + ".log", true));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return log;
    }


}
