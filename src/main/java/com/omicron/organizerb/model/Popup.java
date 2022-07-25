/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 14-Jul-22
 * Time: 21:18
 * =============================================================
 **/

package com.omicron.organizerb.model;

import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

public class Popup implements Initializable {


    // ========================================================================================
    // Fields
    // ========================================================================================

    protected final Stage popupStage;

    private String stylesheet;

    private Pane root;

    private double xOffset = 0;
    private double yOffset = 0;

    // ========================================================================================
    // Constructors
    // ========================================================================================

    public Popup(Stage popupStage) {
        this.popupStage = popupStage;
    }

    // ========================================================================================
    // Getters & Setters
    // ========================================================================================


    public void setStylesheet(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    // ========================================================================================
    // Methods
    // ========================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComponents();
        initializeStage();
        makeWindowDraggable();
    }

    private void initializeStage() {
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        root.getStylesheets().remove(0);
        root.getStylesheets().add(stylesheet);
    }

    private void makeWindowDraggable() {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - xOffset);
            popupStage.setY(event.getScreenY() - yOffset);
        });
    }

    protected void initializeComponents() {

    }

}
