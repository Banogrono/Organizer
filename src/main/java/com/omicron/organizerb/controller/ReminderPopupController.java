/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 15-Jun-22
 * Time: 13:31
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReminderPopupController {

    public static void display(Task task)
    {
        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Reminder");


        Label label1= new Label(task.getTitle());


        Button button1= new Button("Thanks!");


        button1.setOnAction(e -> popupWindow.close());

        VBox layout= new VBox(10);

        layout.getChildren().addAll(label1, button1);

        layout.setAlignment(Pos.CENTER);

        Scene scene1= new Scene(layout, 300, 80);

        popupWindow.setScene(scene1);

        popupWindow.setAlwaysOnTop(true);

        popupWindow.showAndWait();

    }

}
