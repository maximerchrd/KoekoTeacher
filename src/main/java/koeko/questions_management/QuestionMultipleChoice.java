package koeko.questions_management;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Vector;

public class QuestionMultipleChoice {
	private String ID;
	private String SUBJECT;
	private String LEVEL;
	private String QUESTION;

	/**
	 * OPTIONSNUMBER: total number of choices for the answer
	 */
	private int OPTIONSNUMBER;
	/**
	 * NB_CORRECT_ANS: number of correct answers
	 */
	private int NB_CORRECT_ANS;
	private String OPT0;
	private String OPT1;
	private String OPT2;
	private String OPT3;
	private String OPT4;
	private String OPT5;
	private String OPT6;
	private String OPT7;
	private String OPT8;
	private String OPT9;
	private String IMAGE;
	private String QCM_MUID;
	private Timestamp QCM_UPD_TMS;
	private Vector<String> subjects;
	private Vector<String> objectives;
	private Vector <String> answers;
	public QuestionMultipleChoice()	{
		ID="0";
		SUBJECT="";
		LEVEL="";
		QUESTION="";
		OPTIONSNUMBER=0;
		NB_CORRECT_ANS=0;
		OPT0="";
		OPT1="";
		OPT2="";
		OPT3="";
		OPT4="";
		OPT5="";
		OPT6="";
		OPT7="";
		OPT8="";
		OPT9="";
		IMAGE="none";
		answers = new Vector<>();
		subjects = new Vector<>();
		objectives = new Vector<>();
	}
	public QuestionMultipleChoice(String lEVEL, String qUESTION, String oPT0, String oPT1, String oPT2, String oPT3, String oPT4,
								  String oPT5, String oPT6, String oPT7, String oPT8, String oPT9, String iMAGE) {
		
		LEVEL = lEVEL;
		QUESTION = qUESTION;
		OPT0 = oPT0;
		OPT1 = oPT1;
		OPT2 = oPT2;
		OPT3 = oPT3;
		OPT4 = oPT4;
		OPT5 = oPT5;
		OPT6 = oPT6;
		OPT7 = oPT7;
		OPT8 = oPT8;
		OPT9 = oPT9;
		if (iMAGE.length() == 0) {
			IMAGE = "none";
		} else {
			IMAGE = iMAGE;
		}
		int i = 1;
		if (oPT1.length() > 0) i++;
		if (oPT2.length() > 0) i++;
		if (oPT3.length() > 0) i++;
		if (oPT4.length() > 0) i++;
		if (oPT5.length() > 0) i++;
		if (oPT6.length() > 0) i++;
		if (oPT7.length() > 0) i++;
		if (oPT8.length() > 0) i++;
		if (oPT9.length() > 0) i++;
		OPTIONSNUMBER = i;
		subjects = new Vector<>();
		objectives = new Vector<>();
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
	public int getOPTIONSNUMBER() {
		return OPTIONSNUMBER;
	}

	public String getOPT0() {
		return OPT0;
	}
	public String getOPT1() {
		return OPT1;
	}
	public String getOPT2() {
		return OPT2;
	}
	public String getOPT3() {
		return OPT3;
	}
	public String getOPT4() {
		return OPT4;
	}
	public String getOPT5() {
		return OPT5;
	}
	public String getOPT6() {
		return OPT6;
	}
	public String getOPT7() {
		return OPT7;
	}
	public String getOPT8() {
		return OPT8;
	}
	public String getOPT9() {
		return OPT9;
	}
	public int getNB_CORRECT_ANS() {
		return NB_CORRECT_ANS;
	}
	public Vector<String> getAnswers () {
		if (answers.size() < 1) {
			answers.add(OPT0);
			answers.add(OPT1);
			answers.add(OPT2);
			answers.add(OPT3);
			answers.add(OPT4);
			answers.add(OPT5);
			answers.add(OPT6);
			answers.add(OPT7);
			answers.add(OPT8);
			answers.add(OPT9);

			for (int i = 0; i < answers.size(); i++) {
				if (answers.get(i).contentEquals("") || answers.get(i).contentEquals(" ")) {
					answers.remove(i);
					i--;
				}
			}
			return answers;
		} else {
			return answers;
		}
	}
	public Vector<String> getCorrectAnswers () {
		Vector<String> correctAnswers = (Vector<String>) getAnswers().clone();

		for (int i = NB_CORRECT_ANS; i < answers.size(); i++) {
			correctAnswers.remove(NB_CORRECT_ANS);
		}

		return correctAnswers;
	}
	public Vector<String> getIncorrectAnswers () {
		Vector<String> incorrectAnswers = (Vector<String>) getAnswers().clone();
		for (int i = 0; i < NB_CORRECT_ANS; i++) {
			incorrectAnswers.remove(0);
		}

		return incorrectAnswers;
	}

	public String computeShortHashCode() {
		String hash = "";
		String stringToHash = LEVEL + QUESTION + OPTIONSNUMBER + NB_CORRECT_ANS + OPT0 + OPT1 + OPT2 + OPT3 + OPT4
				+ OPT5 + OPT6 + OPT7 + OPT8 + OPT9 + IMAGE;
		for (String subject : subjects) {
			stringToHash += subject;
		}
		for (String objective : objectives) {
			stringToHash += objective;
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(stringToHash.getBytes());
			hash = String.format("%064x", new BigInteger(1, messageDigest.digest()));
			if (hash.length() > 20) {
				hash = hash.substring(hash.length() - 18);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hash;
	}

	public String getIMAGE() {
		return IMAGE;
	}
	public String getQCM_MUID() {
		return QCM_MUID;
	}
	public Timestamp getQCM_UPD_TMS() {
		return QCM_UPD_TMS;
	}
	public Vector<String> getSubjects() {
		return subjects;
	}
	public Vector<String> getObjectives() {
		return objectives;
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
	public void setOPTIONSNUMBER(int oPTIONSNUMBER) {
		OPTIONSNUMBER = oPTIONSNUMBER;
	}
	public void setOPT0(String oPT0) {
		OPT0 = oPT0;
	}
	public void setOPT1(String oPT1) {
		OPT1 = oPT1;
	}
	public void setOPT2(String oPT2) {
		OPT2 = oPT2;
	}
	public void setOPT3(String oPT3) {
		OPT3 = oPT3;
	}
	public void setOPT4(String oPT4) {
		OPT4 = oPT4;
	}
	public void setOPT5(String oPT5) {
		OPT5 = oPT5;
	}
	public void setOPT6(String oPT6) {
		OPT6 = oPT6;
	}
	public void setOPT7(String oPT7) {
		OPT7 = oPT7;
	}
	public void setOPT8(String oPT8) {
		OPT8 = oPT8;
	}
	public void setOPT9(String oPT9) {
		OPT9 = oPT9;
	}
	public void setNB_CORRECT_ANS(int NB_CORRECT_ANS) {
		this.NB_CORRECT_ANS = NB_CORRECT_ANS;
	}
	public void setIMAGE(String iMAGE) {
		if (iMAGE.length() == 0) {
			IMAGE = "none";
		} else {
			IMAGE = iMAGE;
		}
	}
	public void setQCM_MUID(String QCM_MUID) {
		this.QCM_MUID = QCM_MUID;
	}
	public void setQCM_UPD_TMS(Timestamp QCM_UPD_TMS) {
		this.QCM_UPD_TMS = QCM_UPD_TMS;
	}
	public void setSubjects(Vector<String> subjects) {
		this.subjects = subjects;
	}
	public void setObjectives(Vector<String> objectives) {
		this.objectives = objectives;
	}
}
