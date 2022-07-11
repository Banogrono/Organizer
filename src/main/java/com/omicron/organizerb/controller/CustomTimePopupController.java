/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 15-Jun-22
 * Time: 16:30
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalTime;

// TODO: REFACTOR ALL OF THAT
// todo: support for custom css
public class CustomTimePopupController {

    public static void display(Task task)
    {
        Stage popupWindow = new Stage();
        int spacing = 8;

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Time and Date");
        popupWindow.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(spacing);

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            popupWindow.setX(event.getScreenX() - xOffset[0]);
            popupWindow.setY(event.getScreenY() - yOffset[0]);
        });

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        datePicker.setOnAction(e -> {
        });

        HBox timeHBox = new HBox(spacing);

        MenuButton hoursMenuButton = new MenuButton();
        MenuButton minutesMenuButton = new MenuButton();

        initializeHourMenuButton(hoursMenuButton);
        initializeMinutesMenuButton(minutesMenuButton);

        timeHBox.getChildren().addAll(hoursMenuButton, minutesMenuButton);

        HBox otherButtonsHBox = new HBox();
        otherButtonsHBox.setAlignment(Pos.BOTTOM_CENTER);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(hoursMenuButton, Priority.ALWAYS);
        HBox.setHgrow(minutesMenuButton, Priority.ALWAYS);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);


        saveButton.setOnAction(e -> {
            if (task != null) {

                var time = LocalTime.of(
                        Integer.parseInt(hoursMenuButton.getText()),
                        Integer.parseInt(minutesMenuButton.getText()));
                var date = datePicker.getValue();

                task.setTime(time);
                task.setDate(date);
            }

            popupWindow.close();

        });
        cancelButton.setOnAction(e -> popupWindow.close());

        otherButtonsHBox.getChildren().addAll(saveButton, cancelButton);

        timeHBox.setSpacing(spacing);
        timeHBox.setMaxWidth(Double.MAX_VALUE);
        otherButtonsHBox.setSpacing(spacing);
        otherButtonsHBox.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(datePicker, timeHBox, otherButtonsHBox);

        root.setSpacing(spacing);
        root.paddingProperty().setValue(new Insets(spacing,spacing,spacing,spacing));


        Scene scene1 = new Scene(root, spacing*28, spacing*14);

        popupWindow.setScene(scene1);

        popupWindow.showAndWait();

    }

    private static void initializeHourMenuButton(MenuButton hourMenuButton) {
        for (int i = 0; i < 24; i++) {
            var h = i;
            MenuItem hour = new MenuItem("" + h);
            hour.setOnAction(e -> hourMenuButton.textProperty().setValue("" + h));

            hourMenuButton.getItems().add(hour);
            hourMenuButton.setMaxWidth(Double.MAX_VALUE);
            hourMenuButton.textProperty().setValue("" + LocalTime.now().getHour());
        }
    }

    private static void initializeMinutesMenuButton(MenuButton minuteMenuButton) {
        for (int i = 0; i < 60; i+=5) {
            var m = i;
            MenuItem minute = new MenuItem("" + m);
            minute.setOnAction(e -> minuteMenuButton.textProperty().setValue("" + m));

            minuteMenuButton.getItems().add(minute);
            minuteMenuButton.setMaxWidth(Double.MAX_VALUE);
            minuteMenuButton.textProperty().setValue("" + LocalTime.now().getMinute());
        }
    }

}
