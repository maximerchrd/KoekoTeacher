package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import koeko.Koeko;
import koeko.controllers.LeftBar.ClassesControlling.ClassTreeCell;
import koeko.database_management.DbTableHomework;
import koeko.database_management.DbTableRelationSubjectSubject;
import koeko.questions_management.QuestionGeneric;
import koeko.students_management.Classroom;

import java.util.ArrayList;

public class HomeworkListTasks {
    static public void initHomeworkList(ListView<Homework> homeworkListView) {
        homeworkListView.setCellFactory(param -> {
            HomeworkListCell homeworkListCell = new HomeworkListCell();
            return homeworkListCell;
        });

        ArrayList<Homework> homeworks = DbTableHomework.getAllHomeworks();

        for (Homework homework : homeworks) {
            homeworkListView.getItems().add(homework);
        }
    }
}
