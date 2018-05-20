package koeko.GlobalCollectManagment;

import koeko.questions_management.QuestionMultipleChoice;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GCQuestionMultipleChoice {

    public static void QuestionMultipleChoiceFromRecord(QuestionMultipleChoice questionMultipleChoice, ResultSet rs) throws SQLException {
        questionMultipleChoice.setLEVEL(rs.getString("QMC_LEVEL"));
        questionMultipleChoice.setQUESTION(rs.getString("QMC_QUESTION"));
        questionMultipleChoice.setOPT0(rs.getString("QMC_OPTION0"));
        questionMultipleChoice.setOPT1(rs.getString("QMC_OPTION1"));
        questionMultipleChoice.setOPT2(rs.getString("QMC_OPTION2"));
        questionMultipleChoice.setOPT3(rs.getString("QMC_OPTION3"));
        questionMultipleChoice.setOPT4(rs.getString("QMC_OPTION4"));
        questionMultipleChoice.setOPT5(rs.getString("QMC_OPTION5"));
        questionMultipleChoice.setOPT6(rs.getString("QMC_OPTION6"));
        questionMultipleChoice.setOPT7(rs.getString("QMC_OPTION7"));
        questionMultipleChoice.setOPT8(rs.getString("QMC_OPTION8"));
        questionMultipleChoice.setOPT9(rs.getString("QMC_OPTION9"));
        questionMultipleChoice.setNB_CORRECT_ANS(rs.getInt("QMC_NB_CORRECT_ANS"));
        questionMultipleChoice.setQCM_MUID(rs.getString("QMC_MUID"));
        questionMultipleChoice.setIMAGE(rs.getString("QMC_IMAGE_PATH"));
        questionMultipleChoice.setQCM_UPD_TMS(rs.getTimestamp("QMC_UPD_DTS"));
    }

}

