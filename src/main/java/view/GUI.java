package view;

import java.io.IOException;

import controller.KFController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class GUI extends Application {
    Scene scene;
    KFController controller;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Loading FXML file...");
        scene = new Scene(getLoader("SCglParser_v1", stage), 410, 405);
        System.out.println("FXML file loaded successfully.");
        stage.setTitle("SC Kill feed");
        stage.setScene(scene);
        stage.show();
    }

    public Parent getLoader(String fxml, Stage stage) throws Exception {
        Parent loaded = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));
        try {
            loaded = loader.load();
            controller = loader.getController();
            controller.setGUI(this);
            controller.setStage(stage);
            stage.setOnHidden(e -> controller.shutdown());
            controller.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loaded;
    }

    public void setScene(String fxml, int width, int height) throws Exception {
        Stage stage = (Stage) scene.getWindow();
        scene.setRoot(getLoader(fxml, stage));
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    public void refresh(){
        System.out.println("GUi refreshed");
        controller.refresh();
    }
}