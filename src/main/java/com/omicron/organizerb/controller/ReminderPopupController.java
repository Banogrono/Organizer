/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 13-Jul-22
 * Time: 20:27
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.PopupController;
import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.Utility;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ReminderPopupController implements Initializable, PopupController {


    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public Button exitButton;

    @FXML
    public ImageView reminderImage;

    @FXML
    public Label descriptionLabel;

    @FXML
    public VBox innerVBox;

    @FXML
    public HBox innerHBox;

    @FXML
    public VBox outerVBBox;

    @FXML
    public Button okayButton;

    @FXML
    public Label reminderLabel;

    // ----------------- Private fields --------------------------------

    private final Task taskReference;

    private final Stage popupStage;

    private double xOffset = 0;
    private double yOffset = 0;
    private final OrganizerController organizerControllerReference;


    // ========================================================================================
    // Constructors
    // ========================================================================================

    public ReminderPopupController(Stage popupStage, OrganizerController controller, Task taskReference) {
        this.taskReference = taskReference;
        this.popupStage = popupStage;
        this.organizerControllerReference = controller;
    }

    // ========================================================================================
    // Methods
    // ========================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeStage();
        makeWindowDraggable();
        initializePopupImage();
        initializeLabel();
    }

    @FXML
    public void closePopup() {
        popupStage.close();
    }

    private void initializeStage() {
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        outerVBBox.getStylesheets().remove(0);
        outerVBBox.getStylesheets().add(organizerControllerReference.backgroundHBox.getStylesheets().get(0));
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

    private void makeWindowDraggable() {

        outerVBBox.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        outerVBBox.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - xOffset);
            popupStage.setY(event.getScreenY() - yOffset);
        });
    }

}
