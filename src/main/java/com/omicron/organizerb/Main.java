package com.omicron.organizerb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends Application {

    public static Stage mainStageReference;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        mainStageReference = stage;

        Scene scene = getMainScene();

        stage.setTitle("Organizer");
        stage.setScene(scene);
        setApplicationIcon(stage);

        stage.show();

    }

    private Scene getMainScene() throws IOException {
        var sceneFxml = new File("fxml/organizer.fxml").toURI().toURL();

        FXMLLoader fxmlLoader = new FXMLLoader(sceneFxml);
        return new Scene(fxmlLoader.load(), 1100, 600);
    }

    private void setApplicationIcon(Stage stage) throws MalformedURLException {
        URL appIcon = new File("icons/organizer.png").toURI().toURL();
        stage.getIcons().add(new Image(appIcon.toString()));
    }

}