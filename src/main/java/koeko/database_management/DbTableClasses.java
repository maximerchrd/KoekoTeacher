package koeko.database_management;

import koeko.students_management.Classroom;
import koeko.students_management.Student;
import koeko.view.Utilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableClasses {
    static public void createTableClasses(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS classes " +
                    "(ID_CLASS       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_CLASS_GLOBAL      INT     NOT NULL, " +
                    " NAME      TEXT     NOT NULL, " +
                    " LEVEL      TEXT, " +
                    " YEAR      TEXT," +
                    " TYPE      INT NOT NULL," +            // TYPE 0: normal class TYPE 1: group
                    " UNIQUE (NAME) ) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addClass(String name, String level, String year) {
        String sql = 	"INSERT OR IGNORE INTO classes (ID_CLASS_GLOBAL,NAME,LEVEL,YEAR,TYPE) VALUES (?,?,?,?,?)";
        DbUtils.updateWithFiveParam(sql, String.valueOf(2000000), name, level, year, String.valueOf(0));
        sql = "UPDATE classes SET ID_CLASS_GLOBAL = 2000000 + ID_CLASS WHERE ID_CLASS = (SELECT MAX(ID_CLASS) FROM classes)";
        DbUtils.updateWithNoParam(sql);
    }
    static public void addGroupToClass(String groupName, String className) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO classes (ID_CLASS_GLOBAL,NAME,TYPE) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    groupName.replace("'","''") + "','" +
                    1 + "');";
            stmt.executeUpdate(sql);
            sql = "UPDATE classes SET ID_CLASS_GLOBAL = 2000000 + ID_CLASS WHERE ID_CLASS = (SELECT MAX(ID_CLASS) FROM classes)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        DbTableRelationClassClass.addClassGroupRelation(className, groupName);
    }
    static public void updateGroup(String newGroupName, String oldGroupName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE classes SET NAME = '" + newGroupName + "' WHERE NAME = '" + oldGroupName + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        DbTableRelationClassClass.updateClassGroupRelation(newGroupName, oldGroupName);
        DbTableRelationClassQuestion.updateGroupQuestionRelation(newGroupName, oldGroupName);
    }
    static public void updateClass(String newGroupName, String oldGroupName, String date, String level) {
        String sql = "UPDATE classes SET NAME = ?, LEVEL = ?, YEAR = ? WHERE NAME = ?";

        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, newGroupName);
            stmt.setString(2, level);
            stmt.setString(3, date);
            stmt.setString(4, oldGroupName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DbTableRelationClassClass.updateClassGroupRelation(newGroupName, oldGroupName);
        DbTableRelationClassQuestion.updateGroupQuestionRelation(newGroupName, oldGroupName);
    }
    static public ArrayList<String> getGroupsFromClass(String className) {
        ArrayList<String> groups = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT CLASS_NAME2 FROM class_class_relation WHERE CLASS_NAME1='" + className + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                groups.add(rs.getString("CLASS_NAME2"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return groups;
    }

    static public Classroom getClassroomFromName(String name) {
        Classroom classroom = new Classroom();
        String sql = "SELECT YEAR, LEVEL FROM classes WHERE NAME = ?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                classroom.setClassName(name);
                classroom.setClassYear(rs.getString("YEAR"));
                classroom.setClassLevel(rs.getString("LEVEL"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classroom;
    }

    static public ArrayList<Classroom> getClassroomGroupsFromClass(String className) {
        ArrayList<Classroom> groups = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT CLASS_NAME2 FROM class_class_relation WHERE CLASS_NAME1='" + className + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Classroom classroom = new Classroom();
                classroom.setClassName(rs.getString("CLASS_NAME2"));
                groups.add(classroom);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return groups;
    }
    static public List<String> getAllClasses() {
        List<String> classes = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NAME FROM classes WHERE TYPE=0;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                classes.add(rs.getString("NAME"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return classes;
    }

    static public ArrayList<Classroom> getAllClassrooms() {
        ArrayList<Classroom> classes = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NAME FROM classes WHERE TYPE=0;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Classroom classroom = new Classroom();
                classroom.setClassName(rs.getString("NAME"));

                classes.add(classroom);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return classes;
    }

    static public ArrayList<Student> getStudentsInClass(String className) {
        ArrayList<Student> classes = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT students.ID_STUDENT_GLOBAL,FIRST_NAME FROM students " +
                    " INNER JOIN class_students_relation ON students.ID_STUDENT_GLOBAL = class_students_relation.ID_STUDENT_GLOBAL " +
                    " INNER JOIN classes ON classes.ID_CLASS_GLOBAL = class_students_relation.ID_CLASS_GLOBAL " +
                    " WHERE classes.NAME = '" + className + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Student newStudent = new Student();
                newStudent.setName(rs.getString("FIRST_NAME"));
                newStudent.setStudentID(rs.getString("ID_STUDENT_GLOBAL"));
                classes.add(newStudent);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return classes;
    }

    static public void deleteGroup(String groupName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"DELETE FROM classes WHERE NAME='" + groupName + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        DbTableRelationClassClass.deleteClassGroupRelation(groupName);
        DbTableRelationClassQuestion.deleteAllClassQuestionRelationsForClass(groupName);
        DbTableRelationClassStudent.removeAllStudentsFromClass(groupName);
    }
}
