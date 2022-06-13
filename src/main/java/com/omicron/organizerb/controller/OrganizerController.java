package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.RepeatTask;
import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.TaskList;
import com.omicron.organizerb.model.TaskPriority;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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
    public MenuButton remindMeMenuButton;

    @FXML
    public DatePicker addDueDatePicker;

    @FXML
    public MenuButton repeatMenuButton;

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

    @FXML
    public Button markAsDoneButton;

    @FXML
    public ContextMenu taskListContextMenu;

    @FXML
    public ContextMenu categoryContextMenu;

    @FXML
    public TextField addCategoryTextField;


    // -------------------------> private fields

    ArrayList<TaskList> categories;


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Override methods
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setCellFactoryForTaskList(); // experimental
        initializeCategoryContextMenu();
        initializeTaskListContextMenu();
        createAndAddItemsForRepeatMenuButton();
        setActionsForRemindMeMenuButton();
        loadBackgroundsFromDirectory();
    }

    // -------------------------> FXML methods

    private void setCellFactoryForTaskList() {
        activeTasksListView.setCellFactory(lv -> getCustomTaskListCell());
        completedTasksListView.setCellFactory(lv -> getCustomTaskListCell());
    }

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
    public void addCategory(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addCategoryTextField.getText() == null || addCategoryTextField.getText().isBlank())) {

                // create new task
                var taskList = new TaskList();
                taskList.setTaskListTitle(addCategoryTextField.getText());

                categories.add(taskList);

                refreshCategories();

                addCategoryTextField.setText("");
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

    @FXML
    public void setReminderForTask(MouseEvent mouseEvent) {
        // todo implement this
    }

    @FXML
    public void addTaskDueDate(ActionEvent mouseEvent) {
        setTaskDeadLine(addDueDatePicker.getValue());
    }

    @FXML
    public void repeatTask(ActionEvent event) {
        // todo implement this
    }

    @FXML
    public void deleteTask(MouseEvent mouseEvent) {
        // todo refactor
        if (getSelectedTask() != null) {
            deleteSelectedTask();
            return;
        }

        if (getSelectedTaskFromCompletedTasksList() != null) {
            deleteSelectedTaskFromCompletedTasks();
        }
    }

    @FXML
    public void markTaskAsDone(ActionEvent event) {
        var task = getSelectedTask();
        if (task == null) return;

        playDoneJingle();
        getSelectedCategoryItem().getTasks().remove(task);
        refreshTaskList();
        task.setDone(true);
        completedTasksListView.getItems().add(task);
    }


    // -------------------------> internal methods
    private void initializeTaskListContextMenu() {

        // todo: refactor

        var markTaskAsDone = new MenuItem("Done");
        markTaskAsDone.setOnAction(event -> {
            markTaskAsDone(null);
        });


        var moveTask = new Menu("Move task to...");
        for (int i = 0; i < categoriesListView.getItems().size(); i++) {
            var category = (TaskList) categoriesListView.getItems().get(i);

            var menuItem = new MenuItem(category.getTaskListTitle());

            menuItem.setOnAction(e -> {
                var task = getSelectedTask();
                if (task == null) return;

                getSelectedCategoryItem().getTasks().remove(task);
                category.addTask(task);
                refreshTaskList();

            });
            moveTask.getItems().add(menuItem);
        }

        var deleteTask = new MenuItem("Delete task");
        deleteTask.setOnAction(event -> {
            deleteTask(null);
        });


        var setTaskPriority = new Menu("Set priority...");
        for (var priority : TaskPriority.values()) {
            var priorityItem = new MenuItem(priority.name());
            priorityItem.setOnAction(e -> setTaskPriority(priority));
            setTaskPriority.getItems().add(priorityItem);
        }


        taskListContextMenu.getItems().addAll(markTaskAsDone, moveTask, deleteTask, setTaskPriority);
    }

    private void initializeCategoryContextMenu() {

        var deleteCategory = new MenuItem("Delete category");
        deleteCategory.setOnAction(event -> {
            if (getSelectedCategoryItem() == null) return;
            categories.remove(getSelectedCategoryItem());
            categoriesListView.getItems().remove(getSelectedCategoryItem());
        });

        categoryContextMenu.getItems().add(deleteCategory);
    }

    private void setTaskPriority(TaskPriority priority) {
        var task = getSelectedTask();
        if (task == null) return;
        task.setPriority(priority);
        // todo: add some kind of colors to distinguish between tasks with different priorities
    }

    private void createAndAddItemsForRepeatMenuButton() {
        var repeatDaily = new MenuItem("Daily");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.DAILY));

        var repeatWeekly = new MenuItem("Weekly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.WEEKLY));

        var repeatMonthly = new MenuItem("Monthly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.MONTHLY));

        var repeatYearly = new MenuItem("Yearly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.YEARLY));

        var doNotRepeat = new MenuItem("Do not repeat");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.NONE));


        repeatMenuButton.getItems().addAll(doNotRepeat, repeatDaily, repeatWeekly, repeatMonthly, repeatYearly);
    }

    private void setTaskRepetition(Task task, RepeatTask repetition) {
        if (task == null) return;

        task.setRepetition(repetition);
    }

    private void setActionsForRemindMeMenuButton() {

        var remindMeLaterToday = new MenuItem("Later today");
        var remindMeTomorrow = new MenuItem("Tomorrow");
        var remindMeNextWeek = new MenuItem("Next Week");
        var remindMeCustomTime = new MenuItem("Custom");


        // todo: see if that even works
        remindMeLaterToday.setOnAction(e -> setReminder(7200000)); // 2 hours
        remindMeTomorrow.setOnAction(e -> setReminder(LocalDate.now().plusDays(1))); // 1 day
        remindMeNextWeek.setOnAction(e -> setReminder(LocalDate.now().plusDays(7))); // 1 week


        remindMeMenuButton.getItems().addAll(remindMeLaterToday, remindMeTomorrow, remindMeNextWeek, remindMeCustomTime);

    }

    private FXMLLoader getDialogFXMLLoader(String path) {
        try {
            var dialogFXML = new File(path).toURI().toURL();
            return new FXMLLoader(dialogFXML);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // todo: refactor this code
    private void setReminder(long delay) {

        Timer timer = new Timer();
        TimerTask job = new TimerTask() {
            @Override
            public void run() {
                // play remind jingle
                playReminderJingle();
                // todo: set off alert box with task info

            }
        };
        timer.schedule(job, delay);
    }

    private void setReminder(LocalDate date) {

        Timer timer = new Timer();
        TimerTask job = new TimerTask() {
            @Override
            public void run() {
                // play remind jingle
                playReminderJingle();
                // todo: set off alert box with task info

            }
        };

        timer.schedule(job, Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private TaskListCellController getCustomTaskListCell() {
        return TaskListCellController.newInstance();
    }

    private void deleteSelectedTask() {
        if (getSelectedTask() == null) return;
        getSelectedCategoryItem().getTasks().remove(getSelectedTask());
        refreshTaskList();
    }

    private void deleteSelectedTaskFromCompletedTasks() {
        if (getSelectedTaskFromCompletedTasksList() == null) return;
        completedTasksListView.getItems().remove(getSelectedTaskFromCompletedTasksList());
        completedTasksListView.refresh();
    }

    private void playReminderJingle() {
       try {
           var soundFile = new File("src/main/resources/jingels/alarm.mp3");
           playSound(soundFile);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    private void playDoneJingle() {
        try {
            var soundFile = new File("src/main/resources/jingels/done.mp3");
            playSound(soundFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void playSound(File soundFile) throws MalformedURLException {
        Media sound = new Media(soundFile.toURI().toURL().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    private void setTaskDeadLine(LocalDate date) {
        if (getSelectedTask() == null) return;

        getSelectedTask().setDate(date);
    }

    private String[] findBackgroundsInDirectory(String path) {
        var file = new File(path);
        return file.list();
    }

    private void loadBackgroundsFromDirectory() {
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
            var bg = createBackgroundForHBox(new Image(new FileInputStream(pathToBGFolder + image)));
            backgroundHBox.setBackground(bg);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Background createBackgroundForHBox(Image image) {
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

        if (getSelectedTask() == null) return;

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
        if (getSelectedTask() == null) return;
        setContentOfTaskDetails();
        setContentOfTaskTitleLabel();
        setContentOfDatePicker();
    }

    private void setContentOfDatePicker(){
        addDueDatePicker.setValue(getSelectedTask().getDate());
    }

    private void setContentOfCategoryLabel() {
        taskListLabel.setText(getSelectedCategoryItem().getTaskListTitle());
    }

    private void setContentOfTaskTitleLabel() {
        var task = getSelectedTask();

        var title = task.getTitle() == null ? "" : task.getTitle();
        taskTitleLabel.setText(title);
    }

    private void setContentOfTaskDetails() {

        var description = getSelectedTask().getDescription();

        if (description == null || description.isBlank()) {
            taskDescriptionTextArea.setText("");
            return;
        }

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

    private Task getSelectedTaskFromCompletedTasksList () {
        return (Task) completedTasksListView.getSelectionModel().getSelectedItem();
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
