package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PopoutController {

    @FXML private VBox killsList;
    @FXML private Label popoutTitle;
    private KFController parent;
    
    public void setParentController(KFController thisParent){
        parent = thisParent;
    }

    public void startPlayerPopup(){
        popoutTitle.setText("Player kills");
        parent.gameLogParser.getPlayerKills().forEach(kill -> {
            killsList.getChildren().add(0, parent.createTextFlowWithBoldText(kill));
        });
        parent.hidePlayerKillsList();
    }

    public void startShipPopup(){
        popoutTitle.setText("Ship kills");
        parent.gameLogParser.getShipKills().forEach(destruction -> {
            killsList.getChildren().add(0, parent.createTextFlowWithBoldText(destruction));
        });
        parent.hideShipKillsList();
    }
}
