package koeko.controllers.Game;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Window;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.questions_management.QuestionGeneric;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GameController extends Window implements Initializable {
    @FXML
    ListView<Game> GamesList;
    @FXML
    ListView<StudentCellView> TeamOneList;
    @FXML
    ListView<StudentCellView> TeamTwoList;
    @FXML
    TextField endScoreTextView;
    @FXML
    ComboBox teamOnePlayer;
    @FXML
    ComboBox gameType;
    @FXML
    Button startGameButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Koeko.gameControllerSingleton == null) {
            Koeko.gameControllerSingleton = this;
        }

        GamesList.setCellFactory(param -> new GameCell());

        ArrayList<Student> studentsToAdd = new ArrayList<>();
        for (Student student : Koeko.studentGroupsAndClass.get(0).getStudents()) {
            studentsToAdd.add(student);
        }
        for (Game game : Koeko.activeGames) {
            for (StudentCellView studentCellView : game.getTeamOne().getStudentCellView()) {
                studentsToAdd.remove(studentCellView.getStudent());
            }
            for (StudentCellView studentCellView : game.getTeamTwo().getStudentCellView()) {
                studentsToAdd.remove(studentCellView.getStudent());
            }
        }

        for (Student student : studentsToAdd) {
            teamOnePlayer.getItems().add(student.getName());
        }

        TeamOneList.setCellFactory(param -> new StudentCell());
        TeamTwoList.setCellFactory(param -> new StudentCell());

    gameType.getItems().addAll("Send Questions Manually", "Send Questions Automatically (ordered)",
            "Send Questions Automatically (random)", "Game with QR codes");
    }

    public void addStudent(Student student) {
        if (!teamOnePlayer.getItems().contains(student.getName())) {
            teamOnePlayer.getItems().add(student.getName());
        }
    }

    public void addGame() {
        Integer endScore = 30;
        try {
            endScore = Integer.valueOf(endScoreTextView.getText());
        } catch (NumberFormatException e) {
            System.out.println("Unable to read score: wrong format. End score will be default (30)");
        }

        Game newGame = new Game(endScore);
        Koeko.activeGames.add(newGame);


        GamesList.getItems().add(newGame);
    }

    public void addPlayerToOne() {
        Student studentToAdd = getStudentToAdd();
        if (GamesList.getSelectionModel().getSelectedIndex() >= 0) {
            StudentCellView studentCellView = new StudentCellView(studentToAdd, 0);
            Koeko.activeGames.get(GamesList.getSelectionModel().getSelectedIndex()).getTeamOne().getStudentCellView()
                    .add(studentCellView);
            TeamOneList.getItems().add(studentCellView);
        }
    }

    public void addPlayerToTwo() {
        Student studentToAdd = getStudentToAdd();
        if (GamesList.getSelectionModel().getSelectedIndex() >= 0) {
            StudentCellView studentCellView = new StudentCellView(studentToAdd, 0);
            Koeko.activeGames.get(GamesList.getSelectionModel().getSelectedIndex()).getTeamTwo().getStudentCellView()
                    .add(studentCellView);
            TeamTwoList.getItems().add(studentCellView);
        }
    }

    @NotNull
    private Student getStudentToAdd() {
        Student studentToAdd = null;
        for (Student student : Koeko.studentGroupsAndClass.get(0).getStudents()) {
            if (teamOnePlayer.getSelectionModel().getSelectedItem().toString().contentEquals(student.getName())) {
                studentToAdd = student;
                break;
            }
        }
        if (studentToAdd != null && GamesList.getSelectionModel().getSelectedIndex() >= 0) {
            teamOnePlayer.getItems().remove(studentToAdd.getName());
        }
        return studentToAdd;
    }

    public void startGame() {
        if (gameType.getSelectionModel().getSelectedIndex() >=0) {
            startGameButton.setDisable(true);
            NetworkCommunication.networkCommunicationSingleton.activateGame(gameType.getSelectionModel().getSelectedIndex());
        }
    }
}
