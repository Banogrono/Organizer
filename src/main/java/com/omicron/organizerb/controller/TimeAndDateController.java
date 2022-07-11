/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 10-Jul-22
 * Time: 17:26
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeAndDateController {

    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public VBox popupRoot = new VBox();

    @FXML
    public DatePicker datePicker = new DatePicker();

    @FXML
    public MenuButton hoursMenuButton = new MenuButton();

    @FXML
    public MenuButton minutesMenuButton = new MenuButton();

    @FXML
    public Button saveButton = new Button();

    @FXML
    public Button cancelButton = new Button();

    // -------------------------- Private fields ---------------------------------------------

    private final Stage popupStage;
    private final Task taskReference;
    private double xOffset = 0;
    private double yOffset = 0;


    // ========================================================================================
    // Methods
    // ========================================================================================


    public TimeAndDateController(Stage stage, Task task) {
        this.taskReference = task;
        this.popupStage = stage;

        this.datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        initializeDatePicker();
        initializeHourMenuButton();
        initializeMinutesMenuButton();
        setStageProperties();
        makeWindowDraggable();
    }

    @FXML
    public void saveAndClose() {
        saveAndExit();
    }

    @FXML
    public void closePopup() {
        System.out.println(popupStage.getTitle());
        System.out.println(this);

        popupStage.close();
    }

    public static TimeAndDateController timeAndDateControllerFactory(Stage stage, Task task) {
        return new TimeAndDateController(stage, task);
    }


    // -------------------------- Private methods ---------------------------------------------


    private void setStageProperties() {
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Time and Date");
        popupStage.initStyle(StageStyle.UNDECORATED);
    }

    private void makeWindowDraggable() {
        System.out.println("makeWindowDraggable");

        popupRoot.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        popupRoot.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - xOffset);
            popupStage.setY(event.getScreenY() - yOffset);
        });
    }

    private void initializeDatePicker() {
        datePicker.setValue(LocalDate.now());
    }

    private void saveAndExit() {
        if (taskReference != null) {

            var time = LocalTime.of(
                    Integer.parseInt(hoursMenuButton.getText()),
                    Integer.parseInt(minutesMenuButton.getText()));
            var date = datePicker.getValue();

            taskReference.setTime(time);
            taskReference.setDate(date);
        }
        popupStage.close();
    }

    private void initializeHourMenuButton() {
        for (int i = 0; i < 24; i++) {
            var h = i;
            MenuItem hour = new MenuItem("" + h);
            hour.setOnAction(e -> hoursMenuButton.textProperty().setValue("" + h));

            hoursMenuButton.getItems().add(hour);
            hoursMenuButton.setMaxWidth(Double.MAX_VALUE);
            hoursMenuButton.textProperty().setValue("" + LocalTime.now().getHour());
        }
    }

    private void initializeMinutesMenuButton() {


        for (int i = 0; i < 60; i += 5) {
            var m = i;
            MenuItem minute = new MenuItem("" + m);
            minute.setOnAction(e -> minutesMenuButton.textProperty().setValue("" + m));

            minutesMenuButton.getItems().add(minute);
            minutesMenuButton.setMaxWidth(Double.MAX_VALUE);
            minutesMenuButton.textProperty().setValue("" + LocalTime.now().getMinute());
        }
    }

}
