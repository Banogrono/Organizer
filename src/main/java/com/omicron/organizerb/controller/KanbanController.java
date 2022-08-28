package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Controller;
import com.omicron.organizerb.model.ListCellController;
import com.omicron.organizerb.model.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class KanbanController implements Initializable, Controller {

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

    public ListView<Task> targetListView;

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

        todoList.setOnDragEntered(e -> {
            targetListView = todoList;
            System.out.println("todo");

        });

        ongoingList.setOnDragEntered(e -> {
            targetListView = ongoingList;
            System.out.println("ongoing");
        });

        doneList.setOnDragEntered(e -> {
            targetListView = doneList;
            System.out.println("done");
        });


    }

    @FXML
    public void updateTaskDescriptionEventHandler(MouseEvent mouseEvent) {
        updateTaskDescription(getSelectedTask());
    }

    @FXML
    public void deleteTaskEventHandler(MouseDragEvent mouseDragEvent) {
        deleteSelectedTaskAndRefresh(getSelectedTask());
    }

    @FXML
    public void markTaskAsDoneOrActiveEventHandler(MouseDragEvent mouseDragEvent) {
        markTaskAsDoneOrActive();
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
        ListCellController kanbanCell = ListCellController.newInstance("fxml/kanbanCell.fxml");

        if (kanbanCell == null)
            throw new RuntimeException("ListCellController object is a null!");

        // set reference of this object in task list controller, so it can access methods of this object
        kanbanCell.setControllerReference(this);
        return (KanbanListCellController) kanbanCell;
    }

    private void addTask(Task task, ListView<Task> listView) {
        listView.getItems().add(task);
        listView.refresh();
    }

    private Task getSelectedTask() {
        return getSelectedList().getSelectionModel().getSelectedItem();
    }

    public ListView<Task> getSelectedList() {
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

    private void updateTaskDescription(Task selectedTask) {
        String content = taskDescriptionTextArea.getText();

        if (content == null || content.isEmpty() || selectedTask == null) return;

        taskDescriptionTextArea.setText(selectedTask.getDescription());

        getSelectedTask().setDescription(content);
    }

    private void markTaskAsDoneOrActive() {
        Task task = getSelectedTask();
        if (doneList.getItems().contains(task)) {
            task.setDone(false);
            doneList.getItems().remove(task);
            todoList.getItems().add(task);
            return;
        }
        task.setDone(true);
        todoList.getItems().remove(task);
        ongoingList.getItems().remove(task);
        doneList.getItems().add(task);

    }

    private void deleteSelectedTaskAndRefresh(Task selectedTask) {
        todoList.getItems().remove(selectedTask);
        ongoingList.getItems().remove(selectedTask);
        doneList.getItems().remove(selectedTask);
    }

    private void refreshTaskDetails(Task task) {
        if (task == null) return;
        updateTaskDescriptionInDetailsPane(task);
        updateContentOfTaskTitleLabel(task);
        updateContentOfDatePicker(task);
    }


    // todo finish updating task details
    private void updateContentOfDatePicker(Task task) {
    }

    private void updateContentOfTaskTitleLabel(Task task) {

    }

    private void updateTaskDescriptionInDetailsPane(Task task) {

    }
}
