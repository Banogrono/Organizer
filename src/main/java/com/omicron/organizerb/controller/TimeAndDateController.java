/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 10-Jul-22
 * Time: 17:26
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.PopupController;
import com.omicron.organizerb.model.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class TimeAndDateController implements Initializable, PopupController {

    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public VBox popupRoot;

    @FXML
    public DatePicker datePicker;

    @FXML
    public MenuButton hoursMenuButton;

    @FXML
    public MenuButton minutesMenuButton;

    @FXML
    public Button saveButton;

    @FXML
    public Button cancelButton;

    // -------------------------- Private fields ---------------------------------------------

    private final Stage popupStage;
    private final Task taskReference;

    private final OrganizerController organizerControllerReference;
    private double xOffset = 0;
    private double yOffset = 0;

    //  ========================================================================================
    //   Constructors
    //  ========================================================================================

    public TimeAndDateController(Stage stage, OrganizerController controller, Task task) {
        this.taskReference = task;
        this.popupStage = stage;
        this.organizerControllerReference = controller;
    }

    //  ========================================================================================
    //   Methods
    //  ========================================================================================

    @FXML
    public void saveAndClose() {
        saveAndExit();
    }

    @FXML
    public void closePopup() {
        System.out.println(datePicker.getValue());
        popupStage.close();

    }

    // -------------------------- Internal methods ---------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeStage();

        initializeDatePicker();
        initializeMinutesMenuButton();
        initializeHourMenuButton();
        makeWindowDraggable();
    }

    private void initializeStage() {
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupRoot.getStylesheets().remove(0);
        popupRoot.getStylesheets().add(organizerControllerReference.backgroundHBox.getStylesheets().get(0));
    }

    private void makeWindowDraggable() {
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
        LocalDate taskDate = taskReference.getDate();

        if (taskDate == null) {
            datePicker.setValue(LocalDate.now());
        }

        datePicker.setValue(taskDate);
    }

    private void saveAndExit() {
        if (taskReference != null) {

            LocalTime time = LocalTime.of(
                    Integer.parseInt(hoursMenuButton.getText()),
                    Integer.parseInt(minutesMenuButton.getText()));
            LocalDate date = datePicker.getValue();

            taskReference.setTime(time);
            taskReference.setDate(date);
            organizerControllerReference.setReminder(taskReference);
        }
        popupStage.close();
    }

    // todo: think about this, too much redundancy
    private void initializeHourMenuButton() {

        int taskHour = taskReference.getTime().getHour();

        for (int i = 0; i < 24; i++) {
            int h = i;
            MenuItem hour = new MenuItem("" + h);
            hour.setOnAction(e -> hoursMenuButton.textProperty().setValue("" + h));

            hoursMenuButton.getItems().add(hour);
            hoursMenuButton.setMaxWidth(Double.MAX_VALUE);
            hoursMenuButton.textProperty().setValue("" + taskHour);
        }
    }

    private void initializeMinutesMenuButton() {

        int taskMinute = taskReference.getTime().getMinute();

        for (int i = 0; i < 60; i += 5) {
            int m = i;
            MenuItem minute = new MenuItem("" + m);
            minute.setOnAction(e -> minutesMenuButton.textProperty().setValue("" + m));

            minutesMenuButton.getItems().add(minute);
            minutesMenuButton.setMaxWidth(Double.MAX_VALUE);
            minutesMenuButton.textProperty().setValue("" + taskMinute);
        }
    }

}
