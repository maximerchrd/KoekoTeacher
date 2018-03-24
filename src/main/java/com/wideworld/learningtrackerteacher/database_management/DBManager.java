package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;

public class DBManager {

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
		DbTableTests.createTableTest(c,stmt);
		DbTableIndividualObjectiveForStudentResult.createTableDirectEvaluationOfObjective(c,stmt);
		DbTableIndividualQuestionForStudentResult.createTableDirectEvaluationOfObjective(c,stmt);
		DbTableRelationClassObjective.createTableSubject(c,stmt);
		DbTableRelationClassTest.createTableSubject(c,stmt);
		DbTableRelationQuestionTest.createTableRelationQuestionTest(c,stmt);
		DbTableRelationQuestionSubject.createTableSubject(c,stmt);
		DbTableRelationQuestionObjective.createTableRelationQuestionObjective(c,stmt);
		DbTableRelationStudentObjective.createTableSubject(c,stmt);
		DbTableRelationClassStudent.createTableRelationClassStudent(c,stmt);
		DbTableAnswerOptions.createTableAnswerOptions(c,stmt);
		DbTableRelationQuestionAnserOption.createTableSubject(c,stmt);
		DbTableRelationSubjectSubject.createTableRelationSubjectSubject(c,stmt);
		DbTableRelationClassClass.createTableRelationClassClass(c,stmt);
		DbTableRelationClassQuestion.createTableRelationClassQuestion(c,stmt);
		try {
			String sql = "DROP TABLE IF EXISTS 'question'; CREATE TABLE IF NOT EXISTS question " +
					"(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
					" SUBJECT           TEXT    NOT NULL, " +
					" LEVEL      INT     NOT NULL, " +
					" QUESTION           TEXT    NOT NULL, " +
					" ANSWER           TEXT    NOT NULL, " +
					" OPTIONA           TEXT    NOT NULL, " +
					" OPTIONB           TEXT    NOT NULL, " +
					" OPTIONC           TEXT    NOT NULL, " +
					" OPTIOND           TEXT    NOT NULL, " +
					" TRIAL1           TEXT    NOT NULL, " +
					" TRIAL2           TEXT    NOT NULL, " +
					" TRIAL3           TEXT    NOT NULL, " +
					" TRIAL4           TEXT    NOT NULL, " +
					" IMAGE_PATH           TEXT    NOT NULL, " +
					" ID_GLOBAL      INT     NOT NULL) ";
			stmt.executeUpdate(sql);
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}

		try {
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
	/**
	 * get a List of all the QuestionMultipleChoice in the database
	 */
	public List<QuestionMultipleChoice> getAllMultipleChoiceQuestions() throws Exception{
		List<QuestionMultipleChoice> multquestList = new ArrayList<QuestionMultipleChoice>();
		// Select All Query
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM multiple_choice_questions;" );
			while ( rs.next() ) {
				QuestionMultipleChoice quest = new QuestionMultipleChoice();
				//quest.setSUBJECT(rs.getString(2));
				quest.setLEVEL(rs.getString(2));
				quest.setQUESTION(rs.getString(3));
				quest.setOPT0(rs.getString(4));
				quest.setOPT1(rs.getString(5));
				quest.setOPT2(rs.getString(6));
				quest.setOPT3(rs.getString(7));
				quest.setOPT4(rs.getString(8));
				quest.setOPT5(rs.getString(9));
				quest.setOPT6(rs.getString(10));
				quest.setOPT7(rs.getString(11));
				quest.setOPT8(rs.getString(12));
				quest.setOPT9(rs.getString(13));
				quest.setTRIAL0(rs.getString(14));
				quest.setTRIAL1(rs.getString(15));
				quest.setTRIAL2(rs.getString(16));
				quest.setTRIAL3(rs.getString(17));
				quest.setTRIAL4(rs.getString(18));
				quest.setTRIAL5(rs.getString(19));
				quest.setTRIAL6(rs.getString(20));
				quest.setTRIAL7(rs.getString(21));
				quest.setTRIAL8(rs.getString(22));
				quest.setTRIAL9(rs.getString(23));
				quest.setNB_CORRECT_ANS(rs.getInt(25));
				quest.setIMAGE(rs.getString(25));
				quest.setID(rs.getInt(26));
				multquestList.add(quest);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return multquestList;
	}
} 
