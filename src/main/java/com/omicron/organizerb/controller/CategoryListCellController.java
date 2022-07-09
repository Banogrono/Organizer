/**
 * Part of OrganizerB
 * Created by: @Author V
 * Date: @Date 05-Jul-22
 * Time: 00:27
 * =============================================================
 **/

package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.TaskList;
import com.omicron.organizerb.model.Utility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


// TODO: REFACTOR - perhaps even generify

public class CategoryListCellController extends ListCell<TaskList> implements Initializable {


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

    public static CategoryListCellController newInstance() {
        FXMLLoader loader = Utility.getFXMLLoader("fxml/categoryListCell.fxml");
        try {
            loader.load();
            return loader.getController();
        } catch (IOException ex) {
            return null;
        }
    }

    public void setOrganizerControllerReference(OrganizerController reference) {
        this.organizerControllerReference = reference;
    }

}
