/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 05-Jul-22
 * Time: 00:27
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.CustomDataFormat;
import com.omicron.organizerb.model.ListCellController;
import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.TaskList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;


// TODO: REFACTOR - perhaps even generify

public class CategoryListCellController extends ListCell<TaskList> implements Initializable, ListCellController {


    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public HBox categoryHBox;

    @FXML
    public TextField categoryTextField;


    // -------------------------> internal fields
    private TaskList category;

    private OrganizerController organizerControllerReference;


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Overridden methods

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        updateSelected(false);
        addListenerToChildItems();
        setGraphic(categoryHBox); // set root graphic node of our custom list cell
        setDragAndDropBehaviour();
    }


    @Override
    public void commitEdit(TaskList category) {

        category = (category == null) ? this.category : category;

        super.commitEdit(category); // <-- important

        if (this.category == null) return;
        category.setTaskListTitle(categoryTextField.getText());
        organizerControllerReference.refreshMoveTaskMenu();
    }

    @Override
    protected void updateItem(TaskList category, boolean isEmpty) {
        super.updateItem(category, isEmpty);

        makeItemInvisibleIfEmpty(isEmpty);
        if (isEmpty)
            return;
        initializeTextFieldWithData(category);
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
        if (category != null)
            commitEdit(category);

    }

    // -------------------------> internal methods

    // todo: add animations and dragover view
    private void setDragAndDropBehaviour() {

        setOnDragDetected(event -> {

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(CustomDataFormat.CategoryFormat, getItem());
            content.put(DataFormat.PLAIN_TEXT, this.getIndex());
            dragboard.setContent(content);

            event.consume();
        } );

        initializeTaskDragOver();

        initializeTaskDragDropped();


    }

    // todo refactor this madness
    private void initializeTaskDragDropped() {
        setOnDragDropped(dragEvent -> {

            Dragboard db = dragEvent.getDragboard();
            boolean success = false;

            if (db.hasContent(CustomDataFormat.TaskFormat)) {
                Task sourceTask = (Task) dragEvent.getDragboard().getContent(CustomDataFormat.TaskFormat);
                int sourceIndex = (int) dragEvent.getDragboard().getContent(DataFormat.PLAIN_TEXT);

                if (sourceTask.isDone()) {
                    sourceTask.setDone(false);
                    this.category.addTask(sourceTask);
                    organizerControllerReference.completedTasksListView.getItems().remove(sourceIndex);
                    organizerControllerReference.completedTasksListView.refresh();
                } else {
                    this.category.addTask(sourceTask);
                    organizerControllerReference.removeTask(sourceIndex);
                }

                success = true;
            }
            else if (db.hasContent(CustomDataFormat.CategoryFormat)) {
                TaskList sourceList = (TaskList) dragEvent.getDragboard().getContent(CustomDataFormat.CategoryFormat);
                int sourceIndex = (int) dragEvent.getDragboard().getContent(DataFormat.PLAIN_TEXT);
                int targetIndex = this.getIndex();
                TaskList targetList = organizerControllerReference.categoriesListView.getItems().get(targetIndex);

                organizerControllerReference.setTaskList(targetIndex, sourceList);
                organizerControllerReference.setTaskList(sourceIndex, targetList);

                success = true;
            }



            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
    }

    private void initializeTaskDragOver() {
        setOnDragOver(dragEvent -> {

            if (dragEvent.getDragboard().hasContent(CustomDataFormat.TaskFormat)) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            } else if (dragEvent.getDragboard().hasContent(CustomDataFormat.CategoryFormat)) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            }

            dragEvent.consume();
        });
    }

    private void addListenerToChildItems() {
        // add an un-focused listener to each child-item that triggers commitEdit
        categoryHBox.getChildrenUnmodifiable().forEach(child ->
                child.focusedProperty().addListener((obj, prev, curr) -> {
                    if (!curr) {
                        commitEdit(category);
                    }
                }));
    }

    private void initializeTextFieldWithData(TaskList category) {
        this.category = category;
        this.categoryTextField.setText(this.category.getTaskListTitle());
    }

    private void updateItemBasedOnSelectedState(boolean selected) {
        // update UI hints based on selected state
        categoryHBox.getChildrenUnmodifiable().forEach(child -> {

            // setting mouse-transparent to false ensure that the cell will get
            // selected when we click on a field in a non-selected cell
            child.setMouseTransparent(!selected);
            // focus-traversable prevents users from "tabbing" out of the currently selected cell
            child.setFocusTraversable(selected);
        });
    }

    private void makeItemInvisibleIfEmpty(boolean isEmpty) {
        categoryHBox.getChildrenUnmodifiable().forEach(child -> child.setVisible(!isEmpty));
    }

    public void setOrganizerControllerReference(OrganizerController reference) {
        this.organizerControllerReference = reference;
    }

}
