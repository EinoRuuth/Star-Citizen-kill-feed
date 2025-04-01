package controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import view.GUI;
import util.FileController;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import util.GameLogParser;

public class KFController {
    private GameLogParser gameLogParser;
    @FXML VBox PlayerKills, ShipKills; // this way to add multiple of same in cleaner way
    @FXML Button fileButton;
    @FXML TabPane MainTabPane;
    @FXML Slider volumeSlider;
    @FXML TextField usernameField;
    @FXML Label volumeLabel, audioFileErrorMsg, savedUsernameLabel, audioFileLocations, fileErrorMsg, fileLocations;
    File audioFile;
    MediaPlayer player;
    private GUI gui;
    private Stage stage;
    private FileController fileController;
    private final PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
    private final PauseTransition showSaveDuration = new PauseTransition(Duration.seconds(3));

    public void addKill(List<String> kill){
        PlayerKills.getChildren().add(0, createTextFlowWithBoldText(kill));
    }

    public void addDestruction(List<String> destruction){
        ShipKills.getChildren().add(0, createTextFlowWithBoldText(destruction));
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
            PlayerKills.getChildren().add(0, createTextFlowWithBoldText(kill));
        });

        ShipKills.getChildren().clear();
        gameLogParser.getShipKills().forEach(destruction -> {
            ShipKills.getChildren().add(0, createTextFlowWithBoldText(destruction));
        });

        System.out.println("list view filled");
    }

    private TextFlow createTextFlowWithBoldText(List<String> input) {
        TextFlow textFlow = new TextFlow();
        int loopTime = 0;

        for (String s : input) {
            if (loopTime == 0) {
                try {
                    String sanitizedInput = s.replaceAll("[<>]", "");
                    LocalDateTime dateTime = LocalDateTime.parse(sanitizedInput, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
                    String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("(dd.MM) HH.mm.ss"));
                    Text text = new Text(formattedDate+": ");
                    text.setStyle("-fx-font-weight: Bold; -fx-fill: #00aaff;");
                    textFlow.getChildren().add(text);
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid DateTime format: " + s);
                }
            } else if (loopTime % 2 != 0) {
                Text text = new Text(s);
                text.setStyle("-fx-font-weight: Bold; -fx-fill: white;");
                textFlow.getChildren().add(text);
            } else {
                Text text = new Text(s);
                text.setStyle("-fx-font-weight: regular; -fx-fill: lightgrey;");
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
            fileErrorMsg.setVisible(false);
            fileController.setFileLocation(file.getAbsolutePath());
            gameLogParser = new GameLogParser(fileController.getFileLocation(), gui);
            fillListView();
            fileLocations.setText("File: "+fileController.getFileLocation());
        } else {
            fileErrorMsg.setVisible(true);
            fileErrorMsg.setText("file not found");
        }
    }

    @FXML
    private void handleAudioFileChooser(){
        System.out.println("Opened audio file selector");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MP3 files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            fileController.setAudioFileLocation(file.getAbsolutePath());
            audioFileLocations.setText("File: "+fileController.getAudioFileLocation());
            getAlarmAudio();
        } else {
            audioFileErrorMsg.setText("Audio file not found: " + audioFile.getAbsolutePath());
        }
    }

    private void getAlarmAudio(){
        String audioFileLocation = fileController.getAudioFileLocation();
        audioFile = new File(audioFileLocation);
        if (audioFile.exists()) {
            audioFileErrorMsg.setVisible(false);
            Media pick = new Media(audioFile.toURI().toString());
            player = new MediaPlayer(pick);
            double volume = Double.valueOf(fileController.getVolume());
            player.setVolume(volume);
            volumeSlider.setValue(volume*100);
            audioFileLocations.setText("File: "+audioFileLocation);
        } else {
            System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
            audioFileErrorMsg.setVisible(true);
            audioFileErrorMsg.setText("Audio file not found: " + audioFile.getAbsolutePath());
        }
    }

    @FXML
    private void ChangeVolume(){
        int roundedVolume = (int) Math.round(volumeSlider.getValue());
        System.out.println("Volume now: " + roundedVolume);
        volumeLabel.setText(String.valueOf(roundedVolume));
        fileController.setVolume(String.valueOf(roundedVolume / 100.0));
        player.setVolume(roundedVolume / 100.0);
    }

    @FXML
    private void updateVolumeText(){
        int roundedVolume = (int) Math.round(volumeSlider.getValue());
        volumeLabel.setText(String.valueOf(roundedVolume));
    }

    @FXML
    private void playSound(){
        System.out.println("played audio file");
        if (audioFile.exists()) {
            audioFileErrorMsg.setVisible(false);
            if (player.getStatus() == MediaPlayer.Status.PLAYING || player.getStatus() == MediaPlayer.Status.PAUSED) {
                player.stop();
            }
            player.play();
        } else {
            System.err.println("Error playing audofile: " + audioFile.getAbsolutePath());
            audioFileErrorMsg.setVisible(true);
            audioFileErrorMsg.setText("Error playing audofile: " + audioFile.getAbsolutePath());
        }
    }

    private void saveUsername(){
        fileController.setUsername(usernameField.getText());
        savedUsernameLabel.setVisible(true);
        showSaveDuration.setOnFinished(event -> savedUsernameLabel.setVisible(false));
        showSaveDuration.play();
    }

    @FXML
    private void checkUsername(){
        pause.stop();
        pause.setOnFinished(event -> saveUsername());
        pause.play();
    }

    public void start() {
        System.out.println("started");
        audioFileErrorMsg.setVisible(false);
        savedUsernameLabel.setVisible(false);
        fileErrorMsg.setVisible(false);

        MainTabPane.setTabMaxWidth(40);
        MainTabPane.setTabMinWidth(40);
        MainTabPane.setTabMaxHeight(40);
        MainTabPane.setTabMinHeight(40);
        MainTabPane.setSide(Side.LEFT);

        fileController = new FileController();
        String fileLocation = fileController.getFileLocation();
        gameLogParser = new GameLogParser(fileLocation, gui);
        System.out.println("Parser initialized");
        fileLocations.setText("File: "+fileLocation);
        fillListView();
        getAlarmAudio();

        usernameField.setText(fileController.getUsername());
    }

    public void shutdown() {
        gameLogParser.stop();
    }
}
