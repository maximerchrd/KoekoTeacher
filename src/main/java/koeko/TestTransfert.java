package koeko;

import koeko.KoekoSyncCollect.SyncOperations;
import koeko.database_management.DBManager;
import koeko.database_management.DbTableProfessor;
import koeko.view.Professor;
import org.sqlite.date.DateParser;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TestTransfert {

    public static void main(String[] args) throws Exception {

/*        // tests datetime and timestamp
        ZonedDateTime dt = ZonedDateTime.now();
        String sdt = dt.toString();
        ZonedDateTime zdt = ZonedDateTime.parse(sdt);


        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        sdt = ts.toString();
        Timestamp tdt = Timestamp.valueOf(sdt);

        ts = Timestamp.from(zdt.toInstant());

        zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        sdt = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
        tdt = Timestamp.valueOf(sdt);*/


        // Connexion à sqlite
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        // Création de la table prof
        DBManager dbManager = new DBManager();
        dbManager.createTablesIfNotExists();

        // Création du prof si il n'existe pas
        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            System.out.println("no Prof");
            DbTableProfessor.addProfessor("Ali", "Ass");
        } else {
            System.out.println("Welcome professor " + professor.get_alias());
        }

        SyncOperations.SyncAll();

    }

}
