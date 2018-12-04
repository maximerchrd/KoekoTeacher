package koeko.controllers.Game;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
            team1Score.setText(String.valueOf(item.getScoreTeamOne()) + " / " + item.getEndScore());
            Button team1Plus = new Button("+");
            Button team1Minus = new Button("-");
            team1Vbox.getChildren().addAll(team1Score, team1Plus, team1Minus);

            VBox team2Vbox = new VBox(3);
            Label team2Score = new Label();
            team2Score.setText(String.valueOf(item.getScoreTeamTwo()) + " / " + item.getEndScore());
            Button team2Plus = new Button("+");
            Button team2Minus = new Button("-");
            team2Vbox.getChildren().addAll(team2Score, team2Plus, team2Minus);

            hbox.getChildren().addAll(buttonDelete,gameLabel,team1Vbox,team2Vbox);

            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteItem(Game item) {
        System.out.println("implement delete item");
    }
}