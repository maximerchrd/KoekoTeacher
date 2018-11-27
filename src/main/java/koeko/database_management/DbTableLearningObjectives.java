package koeko.database_management;

import koeko.view.Objective;
import koeko.view.Utilities;

import java.sql.*;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableLearningObjectives {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS learning_objectives " +
                    "(ID_OBJECTIVE       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_OBJECTIVE_GLOBAL      INT     NOT NULL, " +
                    " OBJECTIVE      TEXT     NOT NULL, " +
                    " LEVEL_COGNITIVE_ABILITY      INT     NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15)," +
                    " UNIQUE (OBJECTIVE) ); ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    static public void addObjective(String objective, int level_cognitive_ability) {
        String sql = "INSERT OR IGNORE INTO learning_objectives (ID_OBJECTIVE_GLOBAL,OBJECTIVE,LEVEL_COGNITIVE_ABILITY,MODIF_DATE) " +
                "VALUES (?,?,?,?);";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, Utilities.localUniqueID());
            stmt.setString(2, objective);
            stmt.setInt(3, level_cognitive_ability);
            stmt.setString(4, Utilities.TimestampForNowAsString());
            stmt.executeUpdate();
        }  catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    static public void addObjective(Objective objective) throws Exception {

        String sql = 	"INSERT OR IGNORE INTO learning_objectives (ID_OBJECTIVE_GLOBAL,OBJECTIVE,LEVEL_COGNITIVE_ABILITY,IDENTIFIER)" +
                " VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, Utilities.localUniqueID());
            pstmt.setString(2, objective.get_objectiveName());
            pstmt.setInt(3, objective.get_objectiveLevel());
            pstmt.setString(4, objective.get_objectiveMUID());
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public Objective getObjectiveFromIdentifier(String identifier) {
        Objective objective = new Objective();
        String sql = 	"SELECT * FROM learning_objectives WHERE IDENTIFIER = ?";
        try (Connection conn = Utilities.getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, identifier);

            // update
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                objective.set_objectiveName(rs.getString("OBJECTIVE"));
                objective.set_sbjUpdDts(rs.getTimestamp("MODIF_DATE"));
                objective.set_objectiveLevel(rs.getInt("LEVEL_COGNITIVE_ABILITY"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objective;
    }

    static public Vector<String> getObjectiveForQuestionID(String questionID) {
        Vector<String> objectives = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT OBJECTIVE FROM learning_objectives " +
                    "INNER JOIN question_objective_relation ON learning_objectives.ID_OBJECTIVE_GLOBAL = question_objective_relation.ID_OBJECTIVE_GLOBAL " +
                    "INNER JOIN generic_questions ON generic_questions.ID_GLOBAL = question_objective_relation.ID_GLOBAL " +
                    "WHERE generic_questions.ID_GLOBAL = '" + questionID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                objectives.add(rs.getString("OBJECTIVE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return objectives;
    }

    static public Vector<String> getAllObjectives() {
        Vector<String> objectives = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT OBJECTIVE FROM learning_objectives;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                objectives.add(rs.getString("OBJECTIVE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return objectives;
    }

    static public Vector<Objective> getObjectives(String language) {
        Vector<Objective> objectives = new Vector<>();
        String sql = 	"SELECT * FROM learning_objectives";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Objective newObj = new Objective();
                newObj.set_objectiveMUID(rs.getString("IDENTIFIER"));
                newObj.set_objectiveName(rs.getString("OBJECTIVE"));
                newObj.set_objectiveLevel(rs.getInt("LEVEL_COGNITIVE_ABILITY"));
                newObj.set_objectiveLanguage(language);
                objectives.add(newObj);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return objectives;
    }

    static public String getObjectiveIdFromName(String objective) {
        String idObjective = "";

        String sql = "SELECT ID_OBJECTIVE_GLOBAL, IDENTIFIER FROM learning_objectives WHERE OBJECTIVE = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, String.valueOf(objective));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                idObjective = rs.getString("IDENTIFIER");
                if (idObjective == null) {
                    idObjective = rs.getString("ID_OBJECTIVE_GLOBAL");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return idObjective;
    }

    static public void setObjectiveUID(String objectiveName, String objectiveUID) {
        String sql = "UPDATE learning_objectives SET IDENTIFIER=? WHERE OBJECTIVE = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, String.valueOf(objectiveUID));
            pstmt.setString(2, String.valueOf(objectiveName));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateObjective(String oldObjective, String newObjective) {
        String sql = "UPDATE learning_objectives SET OBJECTIVE=? WHERE OBJECTIVE=?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, newObjective);
            pstmt.setString(2, oldObjective);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
