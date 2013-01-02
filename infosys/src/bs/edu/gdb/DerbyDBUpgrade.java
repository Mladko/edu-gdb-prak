package bs.edu.gdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Update der Derby DB von 10.1 auf 10.9
 *
 * @author agribu
 */
public class DerbyDBUpgrade {

    /** Datenbankverbindung */
    private Connection con;

    /** JDBC-Treiber-Pfad */
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    /** Pfad zur Datenbank */
    private static final String URL = "jdbc:derby:/home/agribu/doc/studium/module/gdb/praktikum/Aufgabe6/infosys/gdb-Studienverlaufsplan-DB/gdb-praktikum";
    
    public DerbyDBUpgrade() {

        Properties connProps = new Properties();
        connProps.put("upgrade", "true");

         /* Laden des JDBC-Treibers */
        try {

            Class.forName(DRIVER).newInstance();

        } catch (Exception ex) {

            System.err.println("Der JDBC.Treiber konnte nicht geladen werden.");

        }

        /* Verbindung zur Datenbank herstellen */
        try {

            this.con = DriverManager.getConnection(URL, connProps);

        } catch (SQLException ex1) {

            System.err.println("Die Verbindung zur Datenbank konnte nicht hergestellt werden.\n"
                    + "Die Fehlermeldung lautet: " + ex1.getMessage());
            ex1.printStackTrace();

        }
        
    
    }
    
    public static void main(String[] args) {
        
        new DerbyDBUpgrade();
        
    }
    
}
