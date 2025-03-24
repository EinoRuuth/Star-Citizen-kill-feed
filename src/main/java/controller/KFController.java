package controller;

import java.io.File;
import java.util.List;
import view.GUI;
import util.FileController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.GameLogParser;

public class KFController {
    private GameLogParser gameLogParser;
    @FXML VBox PlayerKills;
    @FXML VBox ShipKills;
    @FXML Button fileButton;
    private GUI gui;
    private Stage stage;
    private FileController fileController;

    public void refresh(){
        System.out.println("controller refreshed");
        fillListView();
    }

    public void setGUI(GUI gui){
        this.gui = gui;
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    private void fillListView() {
        System.out.println("Filling list view...");

        PlayerKills.getChildren().clear();
        gameLogParser.getPlayerKills().forEach(kill -> {
            PlayerKills.getChildren().add(createTextFlowWithBoldText(kill));
        });

        ShipKills.getChildren().clear();
        gameLogParser.getShipKills().forEach(kill -> {
            ShipKills.getChildren().add(createTextFlowWithBoldText(kill));
        });

        System.out.println("list view filled");
    }

    private TextFlow createTextFlowWithBoldText(List<String> input) {
        TextFlow textFlow = new TextFlow();

        int loopTime = 0;

        for (String s : input) {
            if (loopTime == 0) {
                Text text = new Text(s);
                text.setStyle("-fx-font-weight: Bold");
                textFlow.getChildren().add(text);
            }
            else if (loopTime % 2 == 0) {
                Text text = new Text(s);
                text.setStyle("-fx-font-weight: Bold");
                textFlow.getChildren().add(text);
            }
            else {
                Text text = new Text(s);
                text.setStyle("-fx-font-weight: regular");
                textFlow.getChildren().add(text);
            }
            loopTime++;
        }
        return textFlow;
    }

    @FXML
    private void handleFileChooser(){
        System.out.println("Opened file selector");
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            fileController.setFileLocation(file.getAbsolutePath());
            gameLogParser = new GameLogParser(fileController.getFileLocation(), gui);
            fillListView();
        }
    }

    public void start() {
        System.out.println("started");
        fileController = new FileController();
        gameLogParser = new GameLogParser(fileController.getFileLocation(), gui);
        System.out.println("Parser initialized");
        fillListView();
    }

    public void shutdown() {
        System.out.println("shutting down");
        gameLogParser.stop();
    }
}
