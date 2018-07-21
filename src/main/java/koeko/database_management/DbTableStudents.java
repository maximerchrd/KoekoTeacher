package koeko.database_management;

import koeko.ResultsManagement.Result;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Method that adds a student if the name is not already in the table
     *
     * @param address
     * @param name
     * @return the ID of the student OR -1 if there was a problem
     * OR -2 if the student is already in the table and the mac address doesn't correspond to the
     * name already there
     */
    static public String addStudent(String address, String name) {
        String studentID = "-1";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT OR IGNORE INTO students (ID_STUDENT_GLOBAL,MAC_ADDRESS,FIRST_NAME,SURNAME,DATE_BIRTH,QUANTITATIVE_EVAL,QUALITATIVE_EVAL) " +
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
                    studentID = rs.getString("ID_STUDENT_GLOBAL");
                }
            } else {
                String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = '" + name + "';";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    studentID = rs.getString("ID_STUDENT_GLOBAL");
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
                    studentID = "-2";
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
                studentName = rs.getString("FIRST_NAME");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return studentName;
    }

    static public Result getStudentResultsPerSubjectPerTimeStep(String student_name, int timeStep) {
        Result result = new Result();
        Vector<String> student_ids = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + student_name + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                student_ids.add(rs.getString("ID_STUDENT_GLOBAL"));
            }

            int id_student = 0;
            if (!student_ids.isEmpty()) {
                id_student = Integer.parseInt(student_ids.get(0));
            }
            query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL, DATE FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL='" + id_student + "';";
            rs = stmt.executeQuery(query);
            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            Vector<Date> date = new Vector<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                id_questions.add(rs.getString("ID_GLOBAL"));
                evaluations_for_each_question.add(rs.getString("QUANTITATIVE_EVAL"));
                date.add(dateFormat.parse(rs.getString("DATE")));
            }
            //match subjects to each question
            Vector<String> subject_for_question = new Vector<>();
            for (int i = 0; i < id_questions.size(); i++) {
                query = "SELECT SUBJECT FROM subjects INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                        "WHERE question_subject_relation.ID_GLOBAL = '" + id_questions.get(i) + "';";
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    subject_for_question.add(rs.getString("SUBJECT"));
                    //multiplies each evaluation for a specific question by the number of objectives attributed to the question
                    evaluations_for_each_question.insertElementAt(evaluations_for_each_question.get(subject_for_question.size() - 1), subject_for_question.size());
                    date.insertElementAt(date.get(subject_for_question.size() - 1), subject_for_question.size());
                }
                evaluations_for_each_question.remove(subject_for_question.size());
                date.remove(subject_for_question.size());
            }

            collectAndAverageResults(timeStep, result, evaluations_for_each_question, date, subject_for_question);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return result;
    }

    static public Result getStudentResultsPerObjectivePerTimeStep(String student_name, int timeStep) {
        Result result = new Result();
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

            String query = "SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME='" + student_name + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                student_ids.add(rs.getString("ID_STUDENT_GLOBAL"));
            }

            int id_student = 0;
            if (!student_ids.isEmpty()) {
                id_student = Integer.parseInt(student_ids.get(0));
            }
            query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL,DATE FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL='" + id_student + "';";
            rs = stmt.executeQuery(query);
            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            Vector<Date> date = new Vector<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                id_questions.add(rs.getString("ID_GLOBAL"));
                evaluations_for_each_question.add(rs.getString("QUANTITATIVE_EVAL"));
                date.add(dateFormat.parse(rs.getString("DATE")));
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
                    date.insertElementAt(date.get(objectives_for_question.size() - 1), objectives_for_question.size());
                }
                evaluations_for_each_question.remove(objectives_for_question.size());
                date.remove(objectives_for_question.size());
            }


            collectAndAverageResults(timeStep, result, evaluations_for_each_question, date, objectives_for_question);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return result;
    }

    private static void collectAndAverageResults(int timeStep, Result result, Vector<String> evaluations_for_each_question, Vector<Date> date, Vector<String> XValue_for_question) {
        //fill the resultsPerSubject/resultsPerObjective vector corresponding to each subject or objective
        Vector<Vector<Double>> resultsPerObjective = new Vector<>();
        Vector<Vector<Date>> datesPerObjective = new Vector<>();
        for (int i = 0; i < XValue_for_question.size(); i++) {
            if (!result.getXaxisValues().contains(XValue_for_question.get(i))) {
                result.getXaxisValues().add(XValue_for_question.get(i));
                result.getResults().add(new Vector<>());
                result.getDates().add(new Vector<>());

                resultsPerObjective.add(new Vector<>());
                resultsPerObjective.lastElement().add(Double.parseDouble(evaluations_for_each_question.get(i)));
                datesPerObjective.add(new Vector<>());
                datesPerObjective.lastElement().add(date.get(i));
                //results.add(evaluations_for_each_question.get(i));
            } else {
                int index = result.getXaxisValues().indexOf(XValue_for_question.get(i));
                resultsPerObjective.get(index).add(Double.parseDouble(evaluations_for_each_question.get(i)));
                datesPerObjective.get(index).add(date.get(i));
            }
        }
        //loop over subjects or objectives
        for (int i = 0; i < result.getXaxisValues().size(); i++) {
            Date dateNow = new Date();
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(dateNow);
            int year1 = cal1.get(Calendar.YEAR);
            int subyear1;
            if (timeStep == 1) {
                subyear1 = cal1.get(Calendar.WEEK_OF_YEAR);
            } else {
                subyear1 = cal1.get(Calendar.MONTH);
            }
            int counter = 0;
            while (resultsPerObjective.get(i).size() > 0 && counter < 200) {
                Integer numberOfResultsForAveraging = 0;
                Double resultToAverage = 0.0;
                //loop over results for the subject
                for (int j = 0; j < resultsPerObjective.get(i).size(); j++) {
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(datesPerObjective.get(i).get(j));
                    int year2 = cal2.get(Calendar.YEAR);
                    int subyear2;
                    if (timeStep == 1) {
                        subyear2 = cal2.get(Calendar.WEEK_OF_YEAR);
                    } else {
                        subyear2 = cal2.get(Calendar.MONTH);
                    }
                    if (year1 == year2 && subyear1 == subyear2 || timeStep == 0) {
                        resultToAverage += resultsPerObjective.get(i).get(j);
                        numberOfResultsForAveraging++;

                        resultsPerObjective.get(i).remove(j);
                        datesPerObjective.get(i).remove(j);
                        j--;
                    }
                }

                //average result and save it
                result.getResults().get(i).add(String.valueOf(resultToAverage / numberOfResultsForAveraging));
                if (timeStep == 1) {
                    result.getDates().get(i).add(year1 + ", week " + subyear1);
                } else {
                    result.getDates().get(i).add(year1 + ", month " + subyear1);
                }

                //decrease week
                subyear1--;
                if (subyear1 <= 0) {
                    year1--;
                    subyear1 = 53;
                }

                counter++;
            }
            Collections.reverse(result.getResults().get(i));
            Collections.reverse(result.getDates().get(i));
        }
    }
}
