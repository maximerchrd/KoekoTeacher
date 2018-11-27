package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationClassStudent {
    static public void createTableRelationClassStudent(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_students_relation " +
                    "(ID_ST_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_CLASS_GLOBAL      INT     NOT NULL, " +
                    " ID_STUDENT_GLOBAL      INT     NOT NULL, " +
                    " CLASS_STUDENT_CODE      TEXT     NOT NULL, " +
                    " UNIQUE (CLASS_STUDENT_CODE) ) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    static public void addClassStudentRelation(String className, String studentName) {
        String classID = " ";
        String studentID = " ";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_CLASS_GLOBAL FROM classes WHERE NAME='" + className + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                classID = rs.getString("ID_CLASS_GLOBAL");
            }
            query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + studentName + "';";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                studentID = rs.getString("ID_STUDENT_GLOBAL");
            }
            String sql = 	"INSERT OR IGNORE INTO class_students_relation (ID_CLASS_GLOBAL,ID_STUDENT_GLOBAL,CLASS_STUDENT_CODE) " +
                    "VALUES ('" +
                    classID + "','" +
                    studentID + "','" +
                    classID + studentID + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    static public void removeStudentFromClass(String studentName, String className) {
        String classID = " ";
        String studentID = " ";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        int last_id_global = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_CLASS_GLOBAL FROM classes WHERE NAME='" + className + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                classID = rs.getString("ID_CLASS_GLOBAL");
            }
            query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + studentName + "';";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                studentID = rs.getString("ID_STUDENT_GLOBAL");
            }
            String sql = 	"DELETE FROM class_students_relation WHERE CLASS_STUDENT_CODE = '" + classID + studentID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    static public void removeAllStudentsFromClass(String className) {
        String classID = " ";
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_CLASS_GLOBAL FROM classes WHERE NAME='" + className.replace("'","''") + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                classID = rs.getString("ID_CLASS_GLOBAL");
            }
            String sql = 	"DELETE FROM class_students_relation WHERE ID_CLASS_GLOBAL = '" + classID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
