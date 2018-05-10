package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableStudents {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS students " +
                    "(ID_STUDENT       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_STUDENT_GLOBAL      INT     NOT NULL, " +
                    " MAC_ADDRESS      TEXT     NOT NULL, " +
                    " FIRST_NAME      TEXT     NOT NULL, " +
                    " SURNAME      TEXT     NOT NULL, " +
                    " DATE_BIRTH      TEXT     NOT NULL, " +
                    " QUANTITATIVE_EVAL      TEXT     NOT NULL, " +
                    " QUALITATIVE_EVAL           TEXT    NOT NULL, " +
                    " UNIQUE (FIRST_NAME))";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Method that adds a student if the name is not already in the table
     * @param address
     * @param name
     * @return the ID of the student OR -1 if there was a problem
     * OR -2 if the student is already in the table and the mac address doesn't correspond to the
     * name already there
     */
    static public Integer addStudent(String address, String name) {
        Integer studentID = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO students (ID_STUDENT_GLOBAL,MAC_ADDRESS,FIRST_NAME,SURNAME,DATE_BIRTH,QUANTITATIVE_EVAL,QUALITATIVE_EVAL) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    address + "','" +
                    name + "','none','none','none','none');";
            int nbLinesChanged = stmt.executeUpdate(sql);
            if (nbLinesChanged > 0) {
                sql = "UPDATE students SET ID_STUDENT_GLOBAL = 2000000 + ID_STUDENT WHERE ID_STUDENT = (SELECT MAX(ID_STUDENT) FROM students)";
                stmt.executeUpdate(sql);
                String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE ID_STUDENT = (SELECT MAX(ID_STUDENT) FROM students);";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    studentID = rs.getInt("ID_STUDENT_GLOBAL");
                }
            } else {
                String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = '" + name + "';";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    studentID = rs.getInt("ID_STUDENT_GLOBAL");
                }
                query = "SELECT MAC_ADDRESS FROM students WHERE FIRST_NAME = '" + name + "';";
                rs = stmt.executeQuery(query);
                String macAddress = "";
                while (rs.next()) {
                    macAddress = rs.getString("MAC_ADDRESS");
                }
                if (!macAddress.contentEquals(address)) {
                    sql = "UPDATE students SET MAC_ADDRESS = '" + address + "' WHERE ID_STUDENT_GLOBAL = '" + studentID + "';";
                    stmt.executeUpdate(sql);
                    studentID = -2;
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return studentID;
    }
    static public Vector<String> getStudentNames() {
        Vector<String> student_names = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT FIRST_NAME FROM students;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                student_names.add(rs.getString("FIRST_NAME"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return student_names;
    }

    static public String getStudentNameWithID(int studentID) {
        String studentName = "";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT FIRST_NAME FROM students WHERE ID_STUDENT_GLOBAL='" + studentID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                studentName =rs.getString("FIRST_NAME");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return studentName;
    }

    static public Vector<Vector<String>> getStudentResultsPerSubject(String student_name) {
        Vector<String> student_ids = new Vector<>();
        Vector<String> subjects = new Vector<>();
        Vector<String> results = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + student_name +"';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                student_ids.add(rs.getString("ID_STUDENT_GLOBAL"));
            }

            int id_student = 0;
            if (!student_ids.isEmpty()) {
                id_student = Integer.parseInt(student_ids.get(0));
            }
            query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL='" + id_student +"';";
            rs = stmt.executeQuery(query);
            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            while (rs.next()) {
                id_questions.add(rs.getString("ID_GLOBAL"));
                evaluations_for_each_question.add(rs.getString("QUANTITATIVE_EVAL"));
            }
            Vector<String> subject_for_question = new Vector<>();
            for (int i = 0; i < id_questions.size(); i++) {
                query = "SELECT SUBJECT FROM subjects INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                        "WHERE question_subject_relation.ID_GLOBAL = '" + id_questions.get(i) + "';";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    subject_for_question.add(rs.getString("SUBJECT"));
                    //multiplies each evaluation for a specific question by the number of objectives attributed to the question
                    evaluations_for_each_question.insertElementAt(evaluations_for_each_question.get(subject_for_question.size() - 1), subject_for_question.size());
                }
                evaluations_for_each_question.remove(subject_for_question.size());
            }
            for (int i = 0; i < subject_for_question.size(); i++) {
                if (!subjects.contains(subject_for_question.get(i))) {
                    subjects.add(subject_for_question.get(i));
                    results.add(evaluations_for_each_question.get(i));
                } else {
                    int old_result_index = subjects.indexOf(subject_for_question.get(i));
                    double old_result = Double.parseDouble(results.get(old_result_index));
                    old_result += Double.parseDouble(evaluations_for_each_question.get(i));
                    results.set(old_result_index,String.valueOf(old_result));
                }
            }
            for (int i = 0; i < results.size(); i++) {
                double result_for_averaging = Double.parseDouble(results.get(i));
                int number_occurences = Collections.frequency(subject_for_question,subjects.get(i));
                results.set(i,String.valueOf(result_for_averaging/number_occurences));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        Vector<Vector<String>> vectors = new Vector<Vector<String>>();
        vectors.add(subjects);
        vectors.add(results);
        return vectors;
    }

    static public Vector<Vector<String>> getStudentResultsPerObjective(String student_name) {
        Vector<String> student_ids = new Vector<>();
        Vector<String> objectives = new Vector<>();
        Vector<String> results = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + student_name +"';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                student_ids.add(rs.getString("ID_STUDENT_GLOBAL"));
            }

            int id_student = 0;
            if (!student_ids.isEmpty()) {
                id_student = Integer.parseInt(student_ids.get(0));
            }
            query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL='" + id_student +"';";
            rs = stmt.executeQuery(query);
            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            while (rs.next()) {
                id_questions.add(rs.getString("ID_GLOBAL"));
                evaluations_for_each_question.add(rs.getString("QUANTITATIVE_EVAL"));
            }
            Vector<String> objectives_for_question = new Vector<>();
            for (int i = 0; i < id_questions.size(); i++) {
                query = "SELECT OBJECTIVE FROM learning_objectives " +
                        "INNER JOIN question_objective_relation ON learning_objectives.ID_OBJECTIVE_GLOBAL = question_objective_relation.ID_OBJECTIVE_GLOBAL " +
                        "WHERE question_objective_relation.ID_GLOBAL = '" + id_questions.get(i) + "';";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    objectives_for_question.add(rs.getString("OBJECTIVE"));
                    //multiplies each evaluation for a specific question by the number of objectives attributed to the question
                    evaluations_for_each_question.insertElementAt(evaluations_for_each_question.get(objectives_for_question.size() - 1), objectives_for_question.size());
                }
                evaluations_for_each_question.remove(objectives_for_question.size());
            }
            for (int i = 0; i < objectives_for_question.size(); i++) {
                if (!objectives.contains(objectives_for_question.get(i))) {
                    objectives.add(objectives_for_question.get(i));
                    results.add(evaluations_for_each_question.get(i));
                } else {
                    int old_result_index = objectives.indexOf(objectives_for_question.get(i));
                    double old_result = Double.parseDouble(results.get(old_result_index));
                    old_result += Double.parseDouble(evaluations_for_each_question.get(i));
                    results.set(old_result_index,String.valueOf(old_result));
                }
            }
            for (int i = 0; i < results.size(); i++) {
                double result_for_averaging = Double.parseDouble(results.get(i));
                int number_occurences = Collections.frequency(objectives_for_question,objectives.get(i));
                results.set(i,String.valueOf(result_for_averaging/number_occurences));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        Vector<Vector<String>> vectors = new Vector<Vector<String>>();
        vectors.add(objectives);
        vectors.add(results);
        return vectors;
    }
}
