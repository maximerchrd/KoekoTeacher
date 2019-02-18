package koeko.controllers.Game;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;

import java.io.IOException;
import java.util.ArrayList;

public class GameCell extends ListCell<Game> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(Game item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            HBox hbox = new HBox(10);

            double buttonImageSize = 20;

            ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonDelete = new Button();
            buttonDelete.setGraphic(deleteImage);
            buttonDelete.setTooltip(
                    new Tooltip("Delete question")
            );
            buttonDelete.setOnAction((event) -> {
                deleteItem(item);
            });

            Label gameLabel = new Label("Game" + (this.getIndex() + 1));

            VBox team1Vbox = new VBox(3);
            Label team1Score = new Label();
            team1Score.setText(String.valueOf(item.getTeamOne().getTeamScore()) + " / " + item.getEndScore());
            Button team1Plus = new Button("+");
            team1Plus.setOnAction(e -> {
                Platform.runLater(() -> {
                    item.getTeamOne().increaseScore(null, 1.0);
                    scoreChanged(item);
                    this.updateItem(item, false);
                });
            });
            Button team1Minus = new Button("-");
            team1Minus.setOnAction(e -> {
                item.getTeamOne().increaseScore(null, -1.0);
                scoreChanged(item);
                this.updateItem(item, false);
            });
            team1Vbox.getChildren().addAll(team1Score, team1Plus, team1Minus);

            VBox team2Vbox = new VBox(3);
            Label team2Score = new Label();
            team2Score.setText(String.valueOf(item.getTeamTwo().getTeamScore()) + " / " + item.getEndScore());
            Button team2Plus = new Button("+");
            team2Plus.setOnAction(e -> {
                item.getTeamTwo().increaseScore(null, 1.0);
                scoreChanged(item);
                this.updateItem(item, false);
            });
            Button team2Minus = new Button("-");
            team2Minus.setOnAction(e -> {
                item.getTeamTwo().increaseScore(null, -1.0);
                scoreChanged(item);
                this.updateItem(item, false);
            });
            team2Vbox.getChildren().addAll(team2Score, team2Plus, team2Minus);

            hbox.getChildren().addAll(buttonDelete,gameLabel,team1Vbox,team2Vbox);

            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteItem(Game item) {
        if (this.getListView().getSelectionModel().getSelectedIndex() == this.getIndex()) {
            Koeko.gameControllerSingleton.TeamOneList.getItems().clear();
            Koeko.gameControllerSingleton.TeamTwoList.getItems().clear();
        }
        Koeko.activeGames.remove(item);
        this.getListView().getItems().remove(this.getIndex());
    }

    private void scoreChanged(Game game) {
        ArrayList<StudentCellView> allStudentsInGame = new ArrayList<>();
        allStudentsInGame.addAll(game.getTeamOne().getStudentCellViews());
        allStudentsInGame.addAll(game.getTeamTwo().getStudentCellViews());
        for (StudentCellView studentCellView : allStudentsInGame) {
            try {
                NetworkCommunication.networkCommunicationSingleton.sendGameScore(studentCellView.getStudent(),
                        game.getTeamOne().getTeamScore(), game.getTeamTwo().getTeamScore());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
