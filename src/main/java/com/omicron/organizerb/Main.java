package com.omicron.organizerb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        var sceneFxml = new File("src/main/resources/fxml/organizer.fxml").toURI().toURL();
        FXMLLoader fxmlLoader = new FXMLLoader(sceneFxml);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Organizer");
        stage.setScene(scene);
        stage.show();

    }

}