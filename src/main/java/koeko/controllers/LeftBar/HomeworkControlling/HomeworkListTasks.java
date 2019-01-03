package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.scene.control.*;
import koeko.database_management.DbTableHomework;

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
