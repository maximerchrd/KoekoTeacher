package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBManager {
	static public String databaseName = "learning_tracker.db";
	public void createDBIfNotExists() throws Exception {
		// connects to db and create it if necessary
		// closes db afterwards
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
	public void createTablesIfNotExists() throws Exception {
		// First create the table if it doesn't exist
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
			stmt = c.createStatement();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		DbTableQuestionGeneric.createTableQuestionGeneric(c,stmt);
		DbTableQuestionMultipleChoice.createTableQuestionMultipleChoice(c,stmt);
		DbTableQuestionShortAnswer.createTableQuestionShortAnswer(c,stmt);
		DbTableQuestionIntermediateAnswers.createTableQuestionIntermediateAnswers(c,stmt);
		DbTableDirectEvaluationOfObjective.createTableDirectEvaluationOfObjective(c,stmt);
		DbTableLearningObjectives.createTableSubject(c,stmt);
		DbTableClasses.createTableClasses(c,stmt);
		DbTableSubject.createTableSubject(c,stmt);
		DbTableStudents.createTableSubject(c,stmt);
		DbTableTest.createTableTest(c,stmt);
		DbTableIndividualObjectiveForStudentResult.createTableDirectEvaluationOfObjective(c,stmt);
		DbTableIndividualQuestionForStudentResult.createTableDirectEvaluationOfObjective(c,stmt);
		DbTableRelationClassObjective.createTableSubject(c,stmt);
		DbTableRelationClassTest.createTableSubject(c,stmt);
		DbTableRelationQuestionSubject.createTableSubject(c,stmt);
		DbTableRelationQuestionObjective.createTableRelationQuestionObjective(c,stmt);
		DbTableRelationStudentObjective.createTableSubject(c,stmt);
		DbTableRelationClassStudent.createTableRelationClassStudent(c,stmt);
		DbTableAnswerOptions.createTableAnswerOptions(c,stmt);
		DbTableRelationQuestionAnserOption.createTableSubject(c,stmt);
		DbTableRelationSubjectSubject.createTableRelationSubjectSubject(c,stmt);
		DbTableRelationClassClass.createTableRelationClassClass(c,stmt);
		DbTableRelationClassQuestion.createTableRelationClassQuestion(c,stmt);
		DbTableSettings.createTableSettings(c,stmt);
		DbTableRelationObjectiveTest.createTableRelationObjectiveTest(c,stmt);
		DbTableProfessor.createTableProfessor(c, stmt);
		DbTableRelationQuestionQuestion.createTableRelationQuestionQuestion(c, stmt);
		DBTableSyncOp.createTableSyncOp(c, stmt);
		DbTableLogs.createTableClasses(c, stmt);
		try {
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
} 
