package com.omicron.organizerb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    public static Stage mainStageReference;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        mainStageReference = stage;

        URL appIcon = new File("src/main/resources/icons/organizer.png").toURI().toURL();
        var sceneFxml = new File("src/main/resources/fxml/organizer.fxml").toURI().toURL();

        FXMLLoader fxmlLoader = new FXMLLoader(sceneFxml);
        Scene scene = new Scene(fxmlLoader.load(), 1100, 600);

        stage.setTitle("Organizer");
        stage.setScene(scene);
        stage.getIcons().add(new Image(appIcon.toString()));

        stage.show();

    }

}