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
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
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
                case HIGH : {
                    this.priorityLabel.setGraphic(Utility.getIcon("/icons/high.png"));
                    break;
                }
                case NORMAL : {
                    this.priorityLabel.setGraphic(null);
                    break;
                }
                case LOW : {
                    this.priorityLabel.setGraphic(Utility.getIcon("/icons/low.png"));
                    break;
                }
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

            if (!db.hasContent(CustomDataFormat.TaskFormat)) {
                dragEvent.setDropCompleted(false);
                dragEvent.consume();
                return;
            }

            int sourceIndex = (int) dragEvent.getDragboard().getContent(DataFormat.PLAIN_TEXT);
            Task sourceTask = (Task) dragEvent.getDragboard().getContent(CustomDataFormat.TaskFormat);
            int targetIndex = this.getIndex();

            ListView<Task> targetListView = sourceTask.isDone() ?
                    organizerControllerReference.activeTasksListView :
                    organizerControllerReference.completedTasksListView;

            boolean isTaskBeingAdded = targetIndex > targetListView.getItems().size();

            if (isTaskBeingAdded) {
                addTaskToListview(sourceIndex, sourceTask);
            } else {
                swapTaskPlacesInListview(sourceIndex, sourceTask, targetIndex);
            }

            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        });
    }

    private void swapTaskPlacesInListview(int sourceIndex, Task sourceTask, int targetIndex) {
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
    }

    private void addTaskToListview(int sourceIndex, Task sourceTask) {
        if (sourceTask.isDone()) {
            sourceTask.setDone(false);
            organizerControllerReference.addTask(sourceTask);
            organizerControllerReference.activeTasksListView.refresh();

            organizerControllerReference.completedTasksListView.getItems().remove(sourceIndex);
            organizerControllerReference.completedTasksListView.refresh();
        } else {
            sourceTask.setDone(true);
            organizerControllerReference.completedTasksListView.getItems().add(sourceTask);
            organizerControllerReference.completedTasksListView.refresh();

            organizerControllerReference.removeTask(sourceIndex);
        }
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

            if (content.get(CustomDataFormat.TaskFormat) == null)
                return;

            dragboard.setContent(content);

            WritableImage snapshot = this.snapshot(new SnapshotParameters(), null);
            dragboard.setDragView(snapshot);

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

    public void setControllerReference(Controller reference) {
        this.organizerControllerReference = (OrganizerController) reference;
    }

}
