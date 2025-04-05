package controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import view.GUI;
import util.FileController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import util.GameLogParser;

public class KFController {
    public GameLogParser gameLogParser;
    @FXML private GridPane mainGrid;
    @FXML private VBox PlayerKills, ShipKills, partyList, playerFeedContainer, shipFeedContainer;
    @FXML private ScrollPane shipKillContainer, playerKillContainer;
    @FXML private Button fileButton, playerPopoutReturn, shipPopoutReturn;
    @FXML private TabPane MainTabPane;
    @FXML private Slider volumeSlider;
    @FXML private TextField usernameField, addToPartyField;
    @FXML private CheckBox alarmToggle, excludeSelf, excludeParty;
    @FXML private Label volumeLabel, audioFileErrorMsg, savedUsernameLabel, audioFileLocations, fileErrorMsg, fileLocations, addedToPartyLabel;
    private List<String> partyMembers = new ArrayList<String>();
    private String username;
    private Boolean alarmOn;
    private Boolean killPoppedOut = false;
    private Boolean shipPoppedOut = false;
    private Stage poputstage = new Stage();
    private Stage poputstage2 = new Stage();
    private File audioFile;
    private MediaPlayer player;
    private GUI gui;
    private Stage stage;
    private FileController fileController;
    private final PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
    private final PauseTransition showSaveDuration = new PauseTransition(Duration.seconds(2));

    public void addKill(List<String> kill){
        if (!kill.contains(username) && !partyMembers.stream().anyMatch(kill::contains)) {
            playSound();
        }
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

    @FXML
    private void fillListView() {
        System.out.println("Filling list view...");

        PlayerKills.getChildren().clear();
        for (List<String> kill : gameLogParser.getPlayerKills()){
            if (excludeSelf.isSelected()){
                if (kill.get(5).contains(username)){
                    continue;
                }
            }
            if (excludeParty.isSelected()) {
                if (partyMembers.stream().anyMatch(kill.get(5)::contains)){
                    continue;
                }
            }
            PlayerKills.getChildren().add(0, createTextFlowWithBoldText(kill));
        }

        ShipKills.getChildren().clear();
        gameLogParser.getShipKills().forEach(destruction -> {
            ShipKills.getChildren().add(0, createTextFlowWithBoldText(destruction));
        });

        System.out.println("list view filled");
    }

    public TextFlow createTextFlowWithBoldText(List<String> input) {
        TextFlow textFlow = new TextFlow();
        if (!input.contains(username) && !partyMembers.stream().anyMatch(input::contains)) {
            textFlow.setStyle("-fx-background-color: #373737;");
        }
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
            volumeLabel.setText(String.valueOf((int) (volume * 100)));
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
        if (alarmOn){
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
        } else {
            System.out.println("Alarm is toggled off");
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

    @FXML
    private void addToParty(){
        addedToPartyLabel.setVisible(true);
        partyMembers.add(addToPartyField.getText());
        fileController.setParty(String.join(",", partyMembers));
        addToPartyField.clear();
        fillPartyMembersList();
        showSaveDuration.setOnFinished(event -> addedToPartyLabel.setVisible(false));
        showSaveDuration.play();
    }

    private HBox makeTheBox(String name){
        HBox hbox = new HBox();
        hbox.setPrefHeight(25.0);
        hbox.setPrefWidth(200.0);
        hbox.setSpacing(3);

        Label label = new Label(name);
        label.setMaxHeight(26.0);
        label.setMinHeight(26.0);
        label.setPrefHeight(26.0);
        label.setPrefWidth(300.0);
        label.setStyle("-fx-border-color: grey; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-color: #353a45;");
        label.setPadding(new Insets(0, 0, 0, 10.0));

        Button button = new Button("X");
        button.setPrefWidth(27.0);
        button.setPrefHeight(26.0);
        button.setStyle("-fx-background-color: darkred;");
        button.setTextFill(Color.WHITE);
        button.setOnAction(e -> {
            partyMembers.remove(name);
            fileController.setParty(String.join(",", partyMembers));
            fillPartyMembersList();
        });

        hbox.getChildren().addAll(label, button);
        return hbox;
    }

    private void fillPartyMembersList() {
        partyList.getChildren().clear();
        for (String i : partyMembers) {
            if (!i.isEmpty()){
                HBox userBox = makeTheBox(i);
                partyList.getChildren().add(userBox);
            }
        }
    }

    @FXML
    private void toggleAlarm(){
        Boolean toggled = alarmToggle.isSelected();
        alarmOn = toggled;
        fileController.setAlarmToggle(String.valueOf(toggled));
    }

    public void hidePlayerKillsList(){
        switch(killPoppedOut + " " + shipPoppedOut){
            case "true false":
                System.out.println(killPoppedOut+" "+shipPoppedOut);
                mainGrid.getRowConstraints().get(0).setPercentHeight(10);
                mainGrid.getRowConstraints().get(1).setPercentHeight(0);
                mainGrid.getRowConstraints().get(2).setPercentHeight(0);
                mainGrid.getRowConstraints().get(3).setPercentHeight(90);
                playerPopoutReturn.setVisible(true);
                playerFeedContainer.setVisible(false);
                break;
            default: // "true true"
                System.out.println(killPoppedOut+" "+shipPoppedOut);
                mainGrid.getRowConstraints().get(0).setPercentHeight(50);
                mainGrid.getRowConstraints().get(1).setPercentHeight(0);
                mainGrid.getRowConstraints().get(2).setPercentHeight(50);
                mainGrid.getRowConstraints().get(3).setPercentHeight(0);
                shipPopoutReturn.setVisible(true);
                shipFeedContainer.setVisible(false);
                playerPopoutReturn.setVisible(true);
                playerFeedContainer.setVisible(false);
                break;
        }
    }

    public void hideShipKillsList(){
        switch(killPoppedOut + " " + shipPoppedOut){
            case "false true":
                System.out.println(killPoppedOut+" "+shipPoppedOut);
                mainGrid.getRowConstraints().get(0).setPercentHeight(0);
                mainGrid.getRowConstraints().get(1).setPercentHeight(90);
                mainGrid.getRowConstraints().get(2).setPercentHeight(10);
                mainGrid.getRowConstraints().get(3).setPercentHeight(0);
                shipPopoutReturn.setVisible(true);
                shipFeedContainer.setVisible(false);
                break;
            default: // "true true"
                System.out.println(killPoppedOut+" "+shipPoppedOut);
                mainGrid.getRowConstraints().get(0).setPercentHeight(50);
                mainGrid.getRowConstraints().get(1).setPercentHeight(0);
                mainGrid.getRowConstraints().get(2).setPercentHeight(50);
                mainGrid.getRowConstraints().get(3).setPercentHeight(0);
                shipPopoutReturn.setVisible(true);
                shipFeedContainer.setVisible(false);
                playerPopoutReturn.setVisible(true);
                playerFeedContainer.setVisible(false);
                break;
        }
    }

    @FXML
    private void returnKillButton(){
        poputstage.close();
        killPoppedOut = false;
        resetLists();
    }

    @FXML
    private void returnShipButton(){
        poputstage2.close();
        shipPoppedOut = false;
        resetLists();
    }

    public void resetLists(){
        switch(killPoppedOut + " " + shipPoppedOut){
            case "false false":
                System.out.println("false false");
                mainGrid.getRowConstraints().get(0).setPercentHeight(0);
                mainGrid.getRowConstraints().get(1).setPercentHeight(50);
                mainGrid.getRowConstraints().get(2).setPercentHeight(0);
                mainGrid.getRowConstraints().get(3).setPercentHeight(50);
                playerPopoutReturn.setVisible(false);
                shipPopoutReturn.setVisible(false);
                playerFeedContainer.setVisible(true);
                shipFeedContainer.setVisible(true);
                break;
            case "false true":
                System.out.println("false true");
                mainGrid.getRowConstraints().get(0).setPercentHeight(0);
                mainGrid.getRowConstraints().get(1).setPercentHeight(90);
                mainGrid.getRowConstraints().get(2).setPercentHeight(10);
                mainGrid.getRowConstraints().get(3).setPercentHeight(0);
                playerPopoutReturn.setVisible(false);
                playerFeedContainer.setVisible(true);
                shipPopoutReturn.setVisible(true);
                shipFeedContainer.setVisible(false);
                break;
            case "true false":
                System.out.println("true false");
                mainGrid.getRowConstraints().get(0).setPercentHeight(10);
                mainGrid.getRowConstraints().get(1).setPercentHeight(0);
                mainGrid.getRowConstraints().get(2).setPercentHeight(0);
                mainGrid.getRowConstraints().get(3).setPercentHeight(90);
                shipPopoutReturn.setVisible(false);
                shipFeedContainer.setVisible(true);
                playerPopoutReturn.setVisible(true);
                playerFeedContainer.setVisible(false);
                break;
        }
    }

    private void openPopup(Boolean player, Stage thiStage){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poppedOutList.fxml"));
        PopoutController controller;
        try {
            Scene scene = new Scene(loader.load(), 600, 300);
            controller = loader.getController();
            controller.setParentController(this);
            if (player) {
                thiStage.setOnHidden(e -> {
                    killPoppedOut = false;
                    resetLists();
                });
                controller.startPlayerPopup();
                thiStage.setTitle("Player kills");
            } else {
                thiStage.setOnHidden(e -> {
                    shipPoppedOut = false;
                    resetLists();
                });
                controller.startShipPopup();
                thiStage.setTitle("Ship kills");
            }
            thiStage.setScene(scene);
            thiStage.getIcons().add(new Image(GUI.class.getResourceAsStream("/logo.png")));
            thiStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void popoutPlayerKillsWindow(){
        System.out.println("popped out player kills");
        killPoppedOut = true;
        openPopup(true, poputstage);
    }

    @FXML
    private void popoutShipKillsWindow(){
        System.out.println("popped out ship kills");
        shipPoppedOut = true;
        openPopup(false, poputstage2);
    }

    public void start() {
        System.out.println("started");

        audioFileErrorMsg.setVisible(false);
        savedUsernameLabel.setVisible(false);
        fileErrorMsg.setVisible(false);
        addedToPartyLabel.setVisible(false);
        playerPopoutReturn.setVisible(false);
        shipPopoutReturn.setVisible(false);

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

        username = fileController.getUsername();
        usernameField.setText(username);

        partyMembers = new ArrayList<>(List.of(fileController.getParty().split(",")));
        partyMembers.removeIf(String::isEmpty);

        fillListView();
        getAlarmAudio();
        fillPartyMembersList();

        boolean isAlarmOn = Boolean.valueOf(fileController.getAlarmToggle());
        alarmToggle.setSelected(isAlarmOn);
        alarmOn = isAlarmOn;
    }

    public void shutdown() {
        poputstage.close();
        poputstage2.close();
        gameLogParser.stop();
    }
}
