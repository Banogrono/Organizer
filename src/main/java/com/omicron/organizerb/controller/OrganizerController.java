package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.TaskList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class OrganizerController implements Initializable {


    // ========================================================================================
    // Fields
    // ========================================================================================

    // -------------------------> FXML components


    @FXML
    public ListView categoriesListView;

    @FXML
    public Label taskListLabel;

    @FXML
    public ListView activeTasksListView;

    @FXML
    public ListView completedTasksListView;

    @FXML
    public Label taskTitleLabel;

    @FXML
    public TextArea taskDescriptionTextArea;

    @FXML
    public Button remindMeButton;

    @FXML
    public Button addDueDateButton;

    @FXML
    public Button repeatButton;

    @FXML
    public Button deleteButton;

    @FXML
    public Button saveButton;

    @FXML
    public TextField addTaskTextField;

    @FXML
    public HBox backgroundHBox;

    @FXML
    public MenuButton backgroundMenuButton;


    // -------------------------> private fields

    ArrayList<TaskList> categories;


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Override methods
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        loadBackgrounds();
    }

    // -------------------------> FXML methods

    @FXML
    public void loadTasks(MouseEvent mouseEvent) {
        refreshTaskList();
    }

    @FXML
    public void addTask(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addTaskTextField.getText() == null || addTaskTextField.getText().isBlank())) {

                // create new task
                var task = new Task();
                task.setTitle(addTaskTextField.getText());

                addTaskToTaskList(task);

                refreshTaskList();

                clearAddTaskTextFiled();
            }
        }
    }

    @FXML
    public void loadTaskDetails(MouseEvent mouseEvent) {
        refreshTaskDetails();
    }

    @FXML
    public void saveTaskDescription(MouseEvent mouseEvent) {
        saveTaskDescriptionFromTextArea();
    }

    // -------------------------> internal methods

    private String[] findBackgroundsInDirectory(String path) {
        var file = new File(path);
        return file.list();
    }

    private void loadBackgrounds() {
        try {
            // get all backgrounds from background folder
           var pathToBGFolder = "src/main/resources/backgrounds/";
           var backgrounds = findBackgroundsInDirectory(pathToBGFolder);

           // add newly-found backgrounds to the menu button
            for (var image : backgrounds) {

                var item = new MenuItem(image);
                item.setOnAction(event -> setBackground(pathToBGFolder, image));
                backgroundMenuButton.getItems().add(item);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setBackground(String pathToBGFolder, String image) {
        try {
            var bg = createBackground(new Image(new FileInputStream(pathToBGFolder + image)));
            backgroundHBox.setBackground(bg);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Background createBackground(Image image) {
        var size = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);

        return new Background(new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                size));
    }

    private void loadCategories() {
        categories = new ArrayList<>();
        createAndLoadSampleData();

        refreshCategories();
    }

    private void saveTaskDescriptionFromTextArea() {
        String content = taskDescriptionTextArea.getText();
        if (content == null || content.isBlank()) return;

        getSelectedTask().setDescription(content);
        refreshTaskDetails();
    }

    private void refreshCategories () {
        categoriesListView.setItems(FXCollections.observableArrayList(categories));
    }

    private void refreshTaskList () {
        int selectedCategoryIndex = getSelectedCategoryIndex();
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size()) return;

        var tasks = categories.get(selectedCategoryIndex);
        activeTasksListView.setItems(FXCollections.observableArrayList(tasks.getTasks()));
        setContentOfCategoryLabel();
    }

    private void refreshTaskDetails () {
        setContentOfTaskDetails();
        setContentOfTaskTitleLabel();
    }

    private void setContentOfCategoryLabel() {
        taskListLabel.setText(getSelectedCategoryItem().getTaskListTitle());
    }

    private void setContentOfTaskTitleLabel() {
        var task = getSelectedTask();
        if (task == null) return;

        var title = task.getTitle() == null ? "" : task.getTitle();
        taskTitleLabel.setText(title);
    }

    private void setContentOfTaskDetails() {
        if (getSelectedTask() == null) return;
        var description = getSelectedTask().getDescription();
        if (description == null || description.isBlank()) return;

        taskDescriptionTextArea.setText(description);
    }

    private int getSelectedCategoryIndex () {
        return categoriesListView.getSelectionModel().getSelectedIndex();
    }

    private TaskList getSelectedCategoryItem () {
        return (TaskList) categoriesListView.getSelectionModel().getSelectedItem();
    }

    private int getSelectedTaskIndex () {
        return activeTasksListView.getSelectionModel().getSelectedIndex();
    }

    private Task getSelectedTask () {
        return (Task) activeTasksListView.getSelectionModel().getSelectedItem();
    }

    private void clearAddTaskTextFiled() {
        addTaskTextField.setText("");
    }

    private void addTaskToTaskList(Task task) {
        getSelectedCategoryItem().addTask(task);
    }

    // -------------------------> test/ debug methods

    private void createAndLoadSampleData() {

        // create some sample tasks
        var taskA = new Task();
        taskA.setTitle("Do something...");

        var taskB = new Task();
        taskB.setTitle("Do that other thing that you had to do yesterday...");

        var taskC = new Task();
        taskC.setTitle("Do anything you want to do...");


        // create sample taskList objects
        var taskList1 = new TaskList("in");
        taskList1.addTask(taskA);

        var taskList2 = new TaskList("important");
        taskList2.addTask(taskB);

        var taskList3 = new TaskList("Planned");
        taskList3.addTask(taskC);

        // add sample taskList objects to categories object
        categories.add(taskList1);
        categories.add(taskList2);
        categories.add(taskList3);

    }

}
