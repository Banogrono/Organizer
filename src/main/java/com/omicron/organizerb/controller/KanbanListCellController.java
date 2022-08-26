package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.Utility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KanbanListCellController extends ListCell<Task> implements Initializable {


    // ========================================================================================
    // Fields
    // ========================================================================================


    @FXML
    public Label taskTitle;
    @FXML
    public HBox cellRoot;

    // private fields
    // -----------------------------------------------------------

    private Task task;
    private KanbanController controllerReference;


    // overridden Methods
    // -----------------------------------------------------------

    // ========================================================================================
    // Methods
    // ========================================================================================


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateSelected(false);
        addListenerToChildItems();
        setDragAndDropBehaviour();
        taskTitle.setText("dupa");
    }

    @Override
    public void commitEdit(Task newValue) {

        newValue = (newValue == null) ? this.task : newValue;

        super.commitEdit(newValue); // <-- important

        if (this.task == null) return;
        newValue.setDone(task.isDone());
    }

    @Override
    protected void updateItem(Task task, boolean isEmpty) {
        super.updateItem(task, isEmpty);

        makeItemInvisibleIfEmpty(isEmpty);
        if (isEmpty)
            return;

        this.task = task;
        taskTitle.setText(task.getTitle());
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);

        updateItemBasedOnSelectedState(selected);

        // start editing when the cell is selected
        if (selected) {
            startEdit();
            return;
        }

        // commit edits if the cell becomes unselected we're not keeping track of "dirty" state
        // so this will commit changes even to unmodified cells providing that task ain't a null
        if (task != null)
            commitEdit(task);
    }

    // Public Methods
    // -----------------------------------------------------------

    public void setControllerReference(KanbanController reference) {
        this.controllerReference = reference;
    }

    static KanbanListCellController newInstance() {
        FXMLLoader loader = Utility.getFXMLLoader("fxml/kanbanCell.fxml");
        try {
            loader.load();
            return loader.getController();
        } catch (IOException ex) {
            return null;
        }
    }

    // Internal Methods
    // -----------------------------------------------------------

    private void addListenerToChildItems() {
        // add an un-focused listener to each child-item that triggers commitEdit
        cellRoot.getChildrenUnmodifiable().forEach(child ->
                child.focusedProperty().addListener((obj, prev, curr) -> {
                    if (!curr) {
                        commitEdit(task);
                    }
                }));
    }
    private void updateItemBasedOnSelectedState(boolean selected) {
        // update UI hints based on selected state
        cellRoot.getChildrenUnmodifiable().forEach(child -> {

            // setting mouse-transparent to false ensure that the cell will get
            // selected when we click on a field in a non-selected cell
            child.setMouseTransparent(!selected);
            // focus-traversable prevents users from "tabbing" out of the currently selected cell
            child.setFocusTraversable(selected);
        });
    }
    private void makeItemInvisibleIfEmpty(boolean isEmpty) {
        cellRoot.getChildrenUnmodifiable().forEach(child -> child.setVisible(!isEmpty));
    }
    private void setDragAndDropBehaviour() {
        initializeDragDetected();
        initializeDragOver();
        initializeDragDropped();
    }
    private void initializeDragDetected() {

    }
    private void initializeDragOver() {

    }
    private void initializeDragDropped() {
    }



}
