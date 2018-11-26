package koeko.controllers.Game;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Window;
import koeko.Koeko;
import koeko.questions_management.QuestionGeneric;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController extends Window implements Initializable {
    @FXML
    ListView<Game> GamesList;
    @FXML
    ListView<Game> TeamOneList;
    @FXML
    ListView<Game> TeamTwoList;
    @FXML
    TextField endScoreTextView;
    @FXML
    ComboBox teamOnePlayer;
    @FXML
    ComboBox teamTwoPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GamesList.setCellFactory(param -> new GameCell());
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

    }

    public void addPlayerToTwo() {

    }
}
