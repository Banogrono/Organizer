package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class KanbanController implements Initializable {

    // ========================================================================================
    // Fields
    // ========================================================================================

    @FXML
    public VBox root;
    @FXML
    public ListView<Task> todoList;
    @FXML
    public ListView<Task> ongoingList;
    @FXML
    public ListView<Task> doneList;
    @FXML
    public TextField addTaskField;
    @FXML
    public VBox taskDetailsRoot;
    @FXML
    public TextArea taskDescriptionTextArea;
    @FXML
    public DatePicker taskDatePicker;
    @FXML
    public MenuButton remindMenuButton;
    @FXML
    public MenuButton repeatMenuButton;
    @FXML
    public Button doneButton;
    @FXML
    public Button deleteButton;
    @FXML
    public MenuButton settingsMenuButton;

    // -----------------------------------------------------------------------------


    // ========================================================================================
    // Constructors
    // ========================================================================================


    // ========================================================================================
    // Methods
    // ========================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setCustomCellsFactoriesForTaskLists();
        createAndLoadSampleData();


    }

    // -----------------------------------------------------------------------------

    public void onNewTaskAdded(KeyEvent event) {
        handleAddingNewTask(event);
    }

    // -----------------------------------------------------------------------------

    private void setCustomCellsFactoriesForTaskLists() {
        todoList.setCellFactory(lv ->
                getCustomKanbanCell());

        ongoingList.setCellFactory(lv ->
                getCustomKanbanCell());

        doneList.setCellFactory(lv ->
                getCustomKanbanCell());
    }

    private KanbanListCellController getCustomKanbanCell() {
        KanbanListCellController kanbanCell = KanbanListCellController.newInstance();

        if (kanbanCell == null)
            throw new RuntimeException("ListCellController object is a null!");

        // set reference of this object in task list controller, so it can access methods of this object
        kanbanCell.setControllerReference(this);
        return kanbanCell;
    }

    private void addTask(Task task, ListView<Task> listView) {
        listView.getItems().add(task);
        listView.refresh();
    }

    private Task getSelectedTask() {
        return getSelectedList().getSelectionModel().getSelectedItem();
    }

    private ListView<Task> getSelectedList() {
        if (todoList.isFocused()) return todoList;
        if (ongoingList.isFocused()) return ongoingList;
        return doneList;
    }


    private void handleAddingNewTask(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addTaskField.getText() == null || addTaskField.getText().isEmpty())) {
                createAndAddNewTaskToSelectedCategory();
            }
        }
    }

    private void createAndAddNewTaskToSelectedCategory() {
        String taskTitle = addTaskField.getText();
        Task task = new Task(taskTitle);
        addTask(task, todoList);
        addTaskField.clear();
    }


    // -----------------------------------------------------------------------------
    private void createAndLoadSampleData() {

        // create some sample tasks
        Task taskA = new Task();
        taskA.setTitle("Do something...");

        Task taskB = new Task();
        taskB.setTitle("Do that other thing that you had to do yesterday...");

        Task taskC = new Task();
        taskC.setTitle("Do anything you want to do...");

        // ----------------------------------------

        addTask(taskA, todoList);

        addTask(taskB, doneList);

        addTask(taskC, ongoingList);

        System.out.println("Data loaded");
    }
}
