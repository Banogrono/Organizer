/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 13-Jun-22
 * Time: 16:15
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;


// TODO: REFACTOR - perhaps even generify

// Stolen and modified from: https://github.com/NF1198/JavaFXCustomListViewExample
public class TaskListCellController extends ListCell<Task> implements Initializable, ListCellController {


    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public CheckBox taskCheckBox;
    @FXML
    public HBox cellHBox;
    public Label priorityLabel;


    // -------------------------> internal fields
    private Task task;
    private OrganizerController organizerControllerReference;


    // ========================================================================================
    // Methods
    // ========================================================================================


    // -------------------------> Overridden methods

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        updateSelected(false);
        addListenerToChildItems();
        setActionForCheckBox();
        setGraphic(cellHBox); // set root graphic node of our custom list cell
        setDragAndDropBehaviour();

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
        initializeTaskCheckBoxWithData(task);
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


    // -------------------------> Internal methods

    private void setActionForCheckBox() {
        taskCheckBox.setOnAction(e -> {
            if (task == null) return;
            if (taskCheckBox.isSelected()) {
                task.setDone(true);
                organizerControllerReference.markTaskAsDoneOrActiveEventHandler();
            }

        });
    }

    private void setPriorityPaneColor(TaskPriority priority) {

        try {
            switch (priority) {
                case HIGH -> this.priorityLabel.setGraphic(Utility.getIcon("/icons/high.png"));
                case NORMAL -> this.priorityLabel.setGraphic(null);
                case LOW -> this.priorityLabel.setGraphic(Utility.getIcon("/icons/low.png"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load priority icons. " + e);
        }
    }

    private void addListenerToChildItems() {
        // add an un-focused listener to each child-item that triggers commitEdit
        cellHBox.getChildrenUnmodifiable().forEach(child ->
                child.focusedProperty().addListener((obj, prev, curr) -> {
                    if (!curr) {
                        commitEdit(task);
                    }
                }));
    }

    private void initializeTaskCheckBoxWithData(Task task) {
        this.task = task;
        taskCheckBox.setText(task.getTitle());
        taskCheckBox.setSelected(task.isDone());
        setPriorityPaneColor(task.getPriority());
    }

    private void setDragAndDropBehaviour() {
        initializeDragDetected();
        initializeDragOver();
        initializeDragDropped();
    }

    private void initializeDragDropped() {
        setOnDragDropped(dragEvent -> {

            Dragboard db = dragEvent.getDragboard();
            boolean success = false;

            if (db.hasContent(CustomDataFormat.TaskFormat)) {
                int sourceIndex = (int) dragEvent.getDragboard().getContent(DataFormat.PLAIN_TEXT);
                Task sourceTask = (Task) dragEvent.getDragboard().getContent(CustomDataFormat.TaskFormat);

                int targetIndex = this.getIndex();
                Task targetTask = organizerControllerReference.activeTasksListView.getItems().get(targetIndex);


                if (!sourceTask.isDone()) {
                    organizerControllerReference.setTask(targetIndex, sourceTask);
                    organizerControllerReference.setTask(sourceIndex, targetTask);
                } else {
                    sourceTask.setDone(false);
                    organizerControllerReference.addTask(sourceTask);
                    organizerControllerReference.completedTasksListView.getItems().remove(sourceIndex);
                    organizerControllerReference.completedTasksListView.refresh();
                }

                success = true;
            }

            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
    }

    private void initializeDragOver() {
        setOnDragOver(dragEvent -> {

            if (dragEvent.getDragboard().hasContent(CustomDataFormat.TaskFormat)) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            }

            dragEvent.consume();
        });
    }

    private void initializeDragDetected() {
        setOnDragDetected(event -> {

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(CustomDataFormat.TaskFormat, getItem());
            content.put(DataFormat.PLAIN_TEXT, this.getIndex());
            dragboard.setContent(content);

            event.consume();
        });
    }

    private void makeItemInvisibleIfEmpty(boolean isEmpty) {
        cellHBox.getChildrenUnmodifiable().forEach(child -> child.setVisible(!isEmpty));
    }

    private void updateItemBasedOnSelectedState(boolean selected) {
        // update UI hints based on selected state
        cellHBox.getChildrenUnmodifiable().forEach(child -> {

            // setting mouse-transparent to false ensure that the cell will get
            // selected when we click on a field in a non-selected cell
            child.setMouseTransparent(!selected);
            // focus-traversable prevents users from "tabbing" out of the currently selected cell
            child.setFocusTraversable(selected);
        });
    }

    public void setOrganizerControllerReference(OrganizerController reference) {
        this.organizerControllerReference = reference;
    }

}
