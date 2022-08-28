package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Controller;
import com.omicron.organizerb.model.CustomDataFormat;
import com.omicron.organizerb.model.ListCellController;
import com.omicron.organizerb.model.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class KanbanListCellController extends ListCell<Task> implements Initializable, ListCellController {


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
        //taskTitle.setText(this.task.getTitle());
        super.setText(this.task.getTitle());

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
    private void initializeDragOver() {
        setOnDragOver(dragEvent -> {

            if (dragEvent.getDragboard().hasContent(CustomDataFormat.TaskFormat)) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            }
            dragEvent.consume();
        });
    }


    // todo fix odd task appearing in listview
    private void initializeDragDropped() {
        setOnDragDropped(dragEvent -> {

            Dragboard db = dragEvent.getDragboard();

            if (!db.hasContent(CustomDataFormat.TaskFormat)) {
                dragEvent.setDropCompleted(false);
                dragEvent.consume();
                return;
            }

            if (controllerReference.targetListView == null)
                return;

            Task sourceTask = (Task) dragEvent.getDragboard().getContent(CustomDataFormat.TaskFormat);

            if (controllerReference.getSelectedList().getItems().size() == 1)
                controllerReference.getSelectedList().getItems().add(new Task());

            controllerReference.getSelectedList().getItems().remove(sourceTask);
            controllerReference.getSelectedList().refresh();
            controllerReference.targetListView.getItems().add(sourceTask);

            dragEvent.setDropCompleted(true);
            dragEvent.consume();
        });
    }


    @Override
    public void setControllerReference(Controller reference) {
        this.controllerReference = (KanbanController) reference;
    }
}
