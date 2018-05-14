package koeko;

import koeko.KoekoSyncCollect.SyncOperations;
import koeko.database_management.DBManager;
import koeko.database_management.DbTableProfessor;
import koeko.view.Professor;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestTransfert {

    public static void main(String[] args) throws Exception {

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
