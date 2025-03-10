package virus;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Utils{
    public static synchronized void print(Object obj)
    {
        System.out.println(obj);
    }

    // crea el logger y le da un file handler especifico
    public static Logger getLogger(Object obj, String nombre)
    {
        Logger log = Logger.getLogger(obj.getClass().getName() + nombre);
        try{
            FileHandler fh =  new FileHandler( "logs/"  + obj.getClass().getName() + "-" + nombre + ".log", true);
            // fh.setFormatter( new SimpleFormatter() );
            fh.setFormatter( new SimpleFormatter(){
                private final String PATTERN = "yyyy-MM-dd' 'HH:mm:ss"; // no se que es esto -> .SSSXXX";

                @Override
                public String format(final LogRecord record)
                {
                    return String.format(
                            "%1$s %2$-7s %3$s\n",
                            new SimpleDateFormat(PATTERN).format(
                                    new Date(record.getMillis())),
                            record.getLevel().getName(), formatMessage(record));
                }
            });
            log.setUseParentHandlers(false);
            log.addHandler(fh);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return log;
    }
}
