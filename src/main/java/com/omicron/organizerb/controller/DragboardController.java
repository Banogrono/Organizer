package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Controller;
import com.omicron.organizerb.model.Draggable;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class DragboardController implements Initializable, Controller {



    public TextField addTask;
    public Button draw;
    public Canvas canvas;
    public Pane backPane;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTask.setOnKeyReleased(this::handleAddingNewTask);

        var graphicsContext = canvas.getGraphicsContext2D();
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                    graphicsContext.stroke();

                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    graphicsContext.lineTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                    graphicsContext.beginPath();
                    graphicsContext.moveTo(event.getX(), event.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    graphicsContext.lineTo(event.getX(), event.getY());
                    graphicsContext.stroke();
                    graphicsContext.closePath();
                });

    }

    public void clear(GraphicsContext ctx) {
        ctx.setFill(Color.DARKBLUE);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setStroke(Color.ALICEBLUE);
    }


    private void handleAddingNewTask(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addTask.getText() == null || addTask.getText().isEmpty())) {
                createNewTaskNode();
            }
        }
    }

    private void createNewTaskNode() {

        AnchorPane backbone = new AnchorPane();
        Label title = new Label(addTask.getText());

        title.setMaxWidth(144);
        title.setMaxHeight(120);

        backbone.getChildren().add(title);

        backbone.setPrefWidth(120);
        backbone.setPrefHeight(100);

        backbone.setMaxWidth(144);
        backbone.setMaxHeight(120);

        backbone.setLayoutY(backPane.getHeight() / 2);
        backbone.setLayoutX(backPane.getWidth() / 2);

        String style = "-fx-border-style: solid; -fx-padding: 4px; -fx-alignment: center; -fx-background-color: #e8e8e8; -fx-border-radius: 4px; -fx-background-radius: 4px";
        backbone.setStyle(style);
        backbone.setOnMouseReleased(e -> {
            System.out.println("D");
        });



        new Draggable.Nature(backbone);
        backPane.getChildren().add(backbone);
    }
}
