package koeko.questions_management;

import koeko.database_management.DbUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Vector;

public class QuestionShortAnswer {
	private String ID;
	private String SUBJECT;
	private String LEVEL;
	private String QUESTION;
	private ArrayList<String> ANSWERS;
	private String IMAGE;
	private Vector<String> subjects;
	private Vector<String> objectives;
	private String UID;
	private Integer timerSeconds;
	public QuestionShortAnswer()
	{
		ID="0";
		SUBJECT="";
		LEVEL="";
		QUESTION="";
		ANSWERS = null;
		IMAGE="none";
		UID="";
		ANSWERS = new ArrayList<>();
		timerSeconds = -1;
	}
	public QuestionShortAnswer(String sUBJECT, String lEVEL, String qUESTION, String iMAGE) {
		
		SUBJECT = sUBJECT;
		LEVEL = lEVEL;
		QUESTION = qUESTION;
		IMAGE = iMAGE;
		UID="";
		ANSWERS = new ArrayList<>();
		timerSeconds = -1;
	}

	public String computeShortHashCode() {
		String stringToHash = QUESTION + IMAGE + timerSeconds;
		for (String answer : ANSWERS) {
			stringToHash += answer;
		}
		if (subjects != null) {
			for (String subject : subjects) {
				stringToHash += subject;
			}
		}
		if (objectives != null) {
			for (String objective : objectives) {
				stringToHash += objective;
			}
		}

		return DbUtils.getHashCode(stringToHash);
	}

	public String getID()
	{
		return ID;
	}
	public String getSUBJECT() {
		return SUBJECT;
	}
	public String getLEVEL() {
		return LEVEL;
	}
	public String getQUESTION() {
		return QUESTION;
	}
	public ArrayList<String> getANSWER() {
		return ANSWERS;
	}
	public String getIMAGE() {
		return IMAGE;
	}
	public Vector<String> getSubjects() {
		return subjects;
	}
	public Vector<String> getObjectives() {
		return objectives;
	}
	public String getUID() {
		return UID;
	}
	public Integer getTimerSeconds() {
		return timerSeconds;
	}

	public void setID(String id)
	{
		ID=id;
	}
	public void setSUBJECT(String sUBJECT) {
		SUBJECT = sUBJECT;
	}
	public void setLEVEL(String lEVEL) {
		LEVEL = lEVEL;
	}
	public void setQUESTION(String qUESTION) {
		QUESTION = qUESTION;
	}
	public void setANSWER(ArrayList<String> aNSWERS) {
		ANSWERS = aNSWERS;
	}
	public void setIMAGE(String iMAGE) {
		IMAGE = iMAGE;
	}
	public void setSubjects(Vector<String> subjects) {
		this.subjects = subjects;
	}
	public void setObjectives(Vector<String> objectives) {
		this.objectives = objectives;
	}
	public void setUID(String UID) {
		this.UID = UID;
	}
	public void setTimerSeconds(Integer timerSeconds) {
		this.timerSeconds = timerSeconds;
	}
}
