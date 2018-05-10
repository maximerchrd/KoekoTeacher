package koeko.GlobalCollectManagment;

import java.sql.Connection;
import java.sql.DriverManager;

public class GCManagement {

    private Connection connect = null;

    public  GCManagement() {
        // Connexion à mysql
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mysql://localhost/global_collect?"
                    + "user=testuser&password=mysqltest99**");
            connect.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }



}
