/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 13-Jul-22
 * Time: 20:27
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Popup;
import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class ReminderPopupController extends Popup {


    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public VBox innerVBox;

    @FXML
    public HBox innerHBox;

    @FXML
    public VBox outerVBBox;

    @FXML
    public Button exitButton;

    @FXML
    public ImageView reminderImage;

    @FXML
    public Label descriptionLabel;

    @FXML
    public Button okayButton;

    @FXML
    public Label reminderLabel;

    // ----------------- Private fields --------------------------------

    private final Task taskReference;


    // ========================================================================================
    // Constructors
    // ========================================================================================

    public ReminderPopupController(Stage stage, String stylesheet, Task task) {
        super(stage);
        super.setStylesheet(stylesheet);

        this.taskReference = task;
    }

    // ========================================================================================
    // Methods
    // ========================================================================================

    @FXML
    public void closePopup() {
        popupStage.close();
    }

    // -------------------------- Internal methods ---------------------------------------------

    @Override
    protected void initializeComponents() {
        super.setRoot(outerVBBox);

        initializePopupImage();
        initializeLabel();
    }

    private void initializePopupImage() {
        String iconPath = "/icons/remind.png";
        String imageLocation = Objects.requireNonNull(Utility.class.getResource(iconPath)).toExternalForm();
        Image image = new Image(imageLocation);
        reminderImage.setImage(image);
    }

    private void initializeLabel() {
        if (taskReference != null) {
            descriptionLabel.setText(taskReference.getTitle());
            return;
        }
        descriptionLabel.setText("");
    }

}
