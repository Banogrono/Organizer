/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 15-Jun-22
 * Time: 13:31
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ReminderPopupController {

    public static void display(Task task)
    {
        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Reminder");
        popupWindow.initStyle(StageStyle.UNDECORATED);

        Label label1= new Label(task.getTitle());


        Button button1= new Button("Thanks!");


        button1.setOnAction(e -> popupWindow.close());

        final double[] xOffset = {0};
        final double[] yOffset = {0};


        VBox layout= new VBox(10);

        layout.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            }
        });
        layout.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                popupWindow.setX(event.getScreenX() - xOffset[0]);
                popupWindow.setY(event.getScreenY() - yOffset[0]);
            }
        });



        layout.getChildren().addAll(label1, button1);

        layout.setAlignment(Pos.CENTER);

        Scene scene1= new Scene(layout, 300, 80);

        popupWindow.setScene(scene1);

        popupWindow.setAlwaysOnTop(true);

        popupWindow.showAndWait();

    }

}
