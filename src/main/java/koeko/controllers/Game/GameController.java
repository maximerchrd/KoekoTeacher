package koeko.controllers.Game;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.questions_management.QuestionGeneric;
import koeko.students_management.Student;
import org.jetbrains.annotations.NotNull;
import sun.nio.ch.Net;

import java.io.IOException;
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
            GamesList.getItems().add(game);
        }

        for (Student student : studentsToAdd) {
            teamOnePlayer.getItems().add(student.getName());
        }

        TeamOneList.setCellFactory(param -> new StudentCell());
        TeamTwoList.setCellFactory(param -> new StudentCell());

        gameType.getItems().addAll("Send Questions Manually", "Send Questions Automatically (ordered)",
                "Send Questions Automatically (random)", "Game with QR codes");
        gameType.getSelectionModel().select(0);

        Platform.runLater(() -> {
            Stage stage = (Stage) GamesList.getScene().getWindow();
            stage.setOnCloseRequest(t -> Koeko.questionSendingControllerSingleton.gameStage = null);
        });

        //set list selection change listener
        GamesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TeamOneList.getItems().clear();
            for (StudentCellView student : newValue.getTeamOne().getStudentCellViews()) {
                TeamOneList.getItems().add(student);
            }
            TeamTwoList.getItems().clear();
            for (StudentCellView student : newValue.getTeamTwo().getStudentCellViews()) {
                TeamTwoList.getItems().add(student);
            }
            // Your action here
            System.out.println("Selected item changed");
        });
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
            Koeko.activeGames.get(GamesList.getSelectionModel().getSelectedIndex()).getTeamOne().getStudentCellViews()
                    .add(studentCellView);
            TeamOneList.getItems().add(studentCellView);
        }
    }

    public void addPlayerToTwo() {
        Student studentToAdd = getStudentToAdd();
        if (GamesList.getSelectionModel().getSelectedIndex() >= 0) {
            StudentCellView studentCellView = new StudentCellView(studentToAdd, 0);
            Koeko.activeGames.get(GamesList.getSelectionModel().getSelectedIndex()).getTeamTwo().getStudentCellViews()
                    .add(studentCellView);
            TeamTwoList.getItems().add(studentCellView);
        }
    }

    public void addPlayerFromQrCode(String team, String gameId, Student student) {
        Boolean gameExists = false;
        for (int i = 0; i < Koeko.activeGames.size(); i++) {
            if (Koeko.activeGames.get(i).getGameId().contentEquals(gameId)) {
                if (team.contentEquals("1")) {
                    StudentCellView studentCellView = new StudentCellView(student, 0);
                    Koeko.activeGames.get(i).getTeamOne().getStudentCellViews().add(studentCellView);
                    //GamesList.getItems().get(i).getTeamOne().getStudentCellViews().add(studentCellView);
                    TeamOneList.getItems().add(studentCellView);
                } else if (team.contentEquals("2")) {
                    StudentCellView studentCellView = new StudentCellView(student, 0);
                    Koeko.activeGames.get(i).getTeamTwo().getStudentCellViews().add(studentCellView);
                    //GamesList.getItems().get(i).getTeamTwo().getStudentCellViews().add(studentCellView);
                    TeamTwoList.getItems().add(studentCellView);
                } else {
                    System.err.println("addPlayerFromQrCode: error, no corresponding team");
                }
                gameExists = true;
                break;
            }
        }

        if (!gameExists) {
            Game newGame = new Game(30);
            newGame.setGameId(gameId);

            if (team.contentEquals("1")) {
                StudentCellView studentCellView = new StudentCellView(student, 0);
                newGame.getTeamOne().getStudentCellViews().add(studentCellView);
                TeamOneList.getItems().add(studentCellView);
            } else if (team.contentEquals("2")) {
                StudentCellView studentCellView = new StudentCellView(student, 0);
                newGame.getTeamTwo().getStudentCellViews().add(studentCellView);
                TeamTwoList.getItems().add(studentCellView);
            } else {
                System.err.println("addPlayerFromQrCode: error, no corresponding team");
            }

            Koeko.activeGames.add(newGame);
            GamesList.getItems().add(newGame);
        }

        NetworkCommunication.networkCommunicationSingleton.activateGame(GameType.qrCodeGame, student);
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
            NetworkCommunication.networkCommunicationSingleton.activateGame(gameType.getSelectionModel().getSelectedIndex(), null);
        }
    }

    public void scoreIncreased(Double scoreIncrease, Game game, Student student) throws IOException {
        double scoreIncrease2 = scoreIncrease / 100;
        Boolean isInTeamOne = false;
        for (StudentCellView studentCellView : game.getTeamOne().getStudentCellViews()) {
            if (studentCellView.getStudent().getName().contentEquals(student.getName())) {
                isInTeamOne = true;
                game.getTeamOne().increaseScore(studentCellView, scoreIncrease2);
                Platform.runLater(() -> {
                    GamesList.refresh();
                    TeamOneList.refresh();
                });
                break;
            }
        }

        if (!isInTeamOne) {
            for (StudentCellView studentCellView : game.getTeamTwo().getStudentCellViews()) {
                if (studentCellView.getStudent().getName().contentEquals(student.getName())) {
                    game.getTeamTwo().increaseScore(studentCellView, scoreIncrease2);
                    Platform.runLater(() -> {
                        GamesList.refresh();
                        TeamTwoList.refresh();
                    });
                    break;
                }
            }
        }

        ArrayList<StudentCellView> allStudentsInGame = new ArrayList<>();
        allStudentsInGame.addAll(game.getTeamOne().getStudentCellViews());
        allStudentsInGame.addAll(game.getTeamTwo().getStudentCellViews());
        for (StudentCellView studentCellView : allStudentsInGame) {
            NetworkCommunication.networkCommunicationSingleton.sendGameScore(studentCellView.getStudent(),
                    game.getTeamOne().getTeamScore(), game.getTeamTwo().getTeamScore());
        }
    }

    public void studentReady(String studentId) {
        Game checkGame = null;
        for (Game game : Koeko.activeGames) {
            for (StudentCellView studentCellView : game.getAllStudents()) {
                if (studentCellView.getStudent().getUniqueDeviceID().contentEquals(studentId)) {
                    studentCellView.setReady(true);
                    checkGame = game;
                    break;
                }
            }
        }

        if (checkGame != null) {
            Boolean studentsReady = true;
            for (StudentCellView studentCellView : checkGame.getAllStudents()) {
                if (!studentCellView.getReady()) {
                    studentsReady = false;
                    break;
                }
            }

            if (studentsReady) {
                ArrayList<QuestionGeneric> questions = new ArrayList<>(Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems());
                ArrayList<QuestionGeneric> questionSets = new ArrayList<>();
                for (QuestionGeneric questionGeneric : questions) {
                    if (questionGeneric.getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET) {
                        questionSets.add(questionGeneric);
                    }
                }
                if (gameType.getSelectionModel().getSelectedItem().toString().contentEquals("Send Questions Automatically (ordered)")) {
                    checkGame.sendNextQuestion(false, questionSets);
                } else if (gameType.getSelectionModel().getSelectedItem().toString().contentEquals("Send Questions Automatically (random)")) {
                    checkGame.sendNextQuestion(true, questionSets);
                }

                for (StudentCellView studentCellView : checkGame.getAllStudents()) {
                    studentCellView.setReady(false);
                }
            }
        }
    }

    public void activateQuestionIdsToTeam(ArrayList<String> questionIds, Student arg_student) {
        ArrayList<Student> studentsToSendTo = new ArrayList<>();
        Boolean teamFound = false;
        for (Game game : Koeko.activeGames) {
            for (StudentCellView studentCellView : game.getTeamOne().getStudentCellViews()) {
                studentsToSendTo.add(studentCellView.getStudent());
                if (studentCellView.getStudent().getUniqueDeviceID().contentEquals(arg_student.getUniqueDeviceID())) {
                    teamFound = true;
                }
            }
            if (teamFound) {
                break;
            } else {
                studentsToSendTo.clear();
            }
            for (StudentCellView studentCellView : game.getTeamTwo().getStudentCellViews()) {
                studentsToSendTo.add(studentCellView.getStudent());
                if (studentCellView.getStudent().getUniqueDeviceID().contentEquals(arg_student.getUniqueDeviceID())) {
                    teamFound = true;
                }
            }
            if (teamFound) {
                break;
            } else {
                studentsToSendTo.clear();
            }
        }
        
        for (int j = 0; j < studentsToSendTo.size(); j++) {
            ArrayList<Student> singleStudentArray = new ArrayList<>();
            singleStudentArray.add(studentsToSendTo.get(j));
            if (questionIds.size() > 0) {
                NetworkCommunication.networkCommunicationSingleton.sendQuestionID(questionIds.get(j % questionIds.size()), singleStudentArray);
            } else {
                System.err.println("Trying to activate the questions for the team: no corresponding id found");
            }
        }
    }

    public void setQrMode() {
        gameType.getSelectionModel().select(3);
    }
}
