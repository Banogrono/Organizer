package com.omicron.organizerb.controller;

import com.omicron.organizerb.model.RepeatTask;
import com.omicron.organizerb.model.Task;
import com.omicron.organizerb.model.TaskList;
import com.omicron.organizerb.model.TaskPriority;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    public ListView<TaskList> categoriesListView;

    @FXML
    public Label taskListLabel;

    @FXML
    public ListView<Task> activeTasksListView;

    @FXML
    public ListView<Task> completedTasksListView;

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

    final int MAX_ICON_SIZE = 24;


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Overridden methods
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAndRefreshCategories();
        setCellFactoryForActiveAndCompletedTaskList(); // experimental do poprawy

        initializeCategoryContextMenu();
        initializeTaskListContextMenu();
        initializeRepeatMenuButton();
        initializeRemindMeMenuButton();
        setIconsForButtons();
        loadBackgroundsFromDirectory();

        disableTaskRelatedButtonsAndMenus(true);

        // todo: check if that works and then move it into separate method
        for (var cat : categoriesListView.getItems()) {
            for (var task : cat.getTasks()) {
                loadTaskIfRepeated(task);
            }
        }
    }

    // -------------------------> FXML methods

    @FXML
    public void loadTasksEventHandler() {
        disableTaskRelatedButtonsAndMenus(true);
        refreshTaskList();
    }

    @FXML
    public void addTaskEventHandler(KeyEvent event) {
        addNewTaskToSelectedCategory(event);
    }

    @FXML
    public void addCategoryEventHandler(KeyEvent event) {
        addNewCategory(event);
    }

    @FXML
    public void loadTaskDetailsEventHandler() {
        disableButtonsIfSelectedTaskIsNull();
        refreshTaskDetails();
    }

    @FXML
    public void updateTaskDescriptionEventHandler() {
        updateTaskDescription();
    }

    @FXML
    public void addTaskDueDateEventHandler() {
        setTaskDeadLine();
    }

    @FXML
    public void deleteTaskEventHandler() {
        if (getSelectedTask() != null)
            deleteSelectedTaskAndRefresh();

        deleteSelectedTaskFromCompletedTasksAndRefresh();
    }

    @FXML
    public void markTaskAsDoneEventHandler() {
        markTaskAsDoneAndAddToCompletedList();
    }

    // -------------------------> internal methods

    private void disableTaskRelatedButtonsAndMenus(boolean value) {
        deleteButton.disableProperty().setValue(value);
        saveButton.disableProperty().setValue(value);
        remindMeMenuButton.disableProperty().setValue(value);
        repeatMenuButton.disableProperty().setValue(value);
        markAsDoneButton.disableProperty().setValue(value);
        addDueDatePicker.disableProperty().setValue(value);
    }

    private void loadTaskIfRepeated(Task task) {
        if (task == null) return;
        if (Objects.equals(task.getDayOfRepetition(), LocalDate.now())) {
            completedTasksListView.getItems().remove(task);
            completedTasksListView.refresh();

            activeTasksListView.getItems().add(task);
            activeTasksListView.refresh();
        }
    }

    private void markTaskAsDoneAndAddToCompletedList() {
        Task task = getSelectedTask();
        if (task == null) return;

        task.setDone(true);
        getSelectedCategoryItem().getTasks().remove(task);
        completedTasksListView.getItems().add(task);

        playDoneJingle();
        refreshTaskList();

    }

    private void setTaskDeadLine() {
        if (addDueDatePicker.getValue() != null)
            setTaskDeadLine(addDueDatePicker.getValue());
    }

    private void addNewTaskToSelectedCategory(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addTaskTextField.getText() == null || addTaskTextField.getText().isBlank())) {

                // create new task
                Task task = new Task();
                task.setTitle(addTaskTextField.getText());

                addTaskToTaskList(task);
                refreshTaskList();
                clearAddTaskTextFiled();
            }
        }
    }

    private void addNewCategory(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addCategoryTextField.getText() == null || addCategoryTextField.getText().isBlank())) {

                // create new taskList, aka category
                TaskList taskList = new TaskList();

                taskList.setTaskListTitle(addCategoryTextField.getText());
                categories.add(taskList);
                refreshCategories();
                addCategoryTextField.setText("");
            }
        }
    }

    private void setCellFactoryForActiveAndCompletedTaskList() {
        activeTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
        completedTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
    }

    private void setIconsForButtons() {

        setIconForNodesExtendingButtonBase(saveButton, "src/main/resources/icons/save.png");
        setIconForNodesExtendingButtonBase(markAsDoneButton, "src/main/resources/icons/done.png");
        setIconForNodesExtendingButtonBase(deleteButton, "src/main/resources/icons/delete.png");

        setIconForNodesExtendingButtonBase(remindMeMenuButton, "src/main/resources/icons/remind.png");
        setIconForNodesExtendingButtonBase(repeatMenuButton, "src/main/resources/icons/repeat.png");
        setIconForNodesExtendingButtonBase(backgroundMenuButton, "src/main/resources/icons/background.png");
    }

    private void setIconForNodesExtendingButtonBase(ButtonBase buttonBase, String pathToIcon) {
        try {
            buttonBase.setGraphic(getIcon(pathToIcon));
            buttonBase.alignmentProperty().setValue(Pos.BOTTOM_LEFT);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageView getIcon(String path) throws MalformedURLException {
        ImageView img = new ImageView(new Image(new File(path).toURI().toURL().toString()));
        img.fitWidthProperty().setValue(MAX_ICON_SIZE);
        img.fitHeightProperty().setValue(MAX_ICON_SIZE);
        return img;
    }

    private void initializeTaskListContextMenu() {

        MenuItem markTaskAsDone = initializeMarkTaskAsDoneMenuItem();
        Menu moveTask = initializeMoveTaskToMenu();
        MenuItem deleteTask = initializeDeleteTaskMenuItem();
        Menu setTaskPriority = initializeSetTaskPriorityMenu();

        taskListContextMenu.getItems().addAll(markTaskAsDone, moveTask, deleteTask, setTaskPriority);
    }

    private Menu initializeSetTaskPriorityMenu() {
        Menu setTaskPriority = new Menu("Set priority...");

        for (var priority : TaskPriority.values()) {
            MenuItem priorityItem = new MenuItem(priority.name());
            priorityItem.setOnAction(e -> setPriorityForSelectedTask(priority));
            setTaskPriority.getItems().add(priorityItem);
        }

        return setTaskPriority;
    }

    private MenuItem initializeDeleteTaskMenuItem() {
        MenuItem deleteTask = new MenuItem("Delete task");

        deleteTask.setOnAction(event -> deleteTaskEventHandler());
        return deleteTask;
    }

    private MenuItem initializeMarkTaskAsDoneMenuItem() {
        MenuItem markTaskAsDone = new MenuItem("Done");

        markTaskAsDone.setOnAction(event -> markTaskAsDoneEventHandler());
        return markTaskAsDone;
    }

    private Menu initializeMoveTaskToMenu() {
        Menu moveTask = new Menu("Move task to...");

        for (int i = 0; i < categoriesListView.getItems().size(); i++) {

            TaskList category = categoriesListView.getItems().get(i);

            MenuItem menuItem = new MenuItem(category.getTaskListTitle());

            menuItem.setOnAction(e -> {
                Task task = getSelectedTask();
                if (task == null) return;

                getSelectedCategoryItem().getTasks().remove(task);
                category.addTask(task);
                refreshTaskList();
            });
            moveTask.getItems().add(menuItem);
        }
        return moveTask;
    }

    private void initializeCategoryContextMenu() {

        MenuItem deleteCategoryMenuItem = new MenuItem("Delete category");
        deleteCategoryMenuItem.setOnAction(event -> deleteCategory());

        categoryContextMenu.getItems().add(deleteCategoryMenuItem);
    }

    private void deleteCategory() {
        if (getSelectedCategoryItem() == null) return;

        categories.remove(getSelectedCategoryItem());
        categoriesListView.getItems().remove(getSelectedCategoryItem());
    }

    private void setPriorityForSelectedTask(TaskPriority priority) {
        Task task = getSelectedTask();
        if (task == null) return;

        task.setPriority(priority);
        // todo: add some kind of colors to distinguish between tasks with different priorities
    }

    private void initializeRepeatMenuButton() {
        MenuItem repeatDaily = new MenuItem("Daily");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.DAILY));

        MenuItem repeatWeekly = new MenuItem("Weekly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.WEEKLY));

        MenuItem repeatMonthly = new MenuItem("Monthly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.MONTHLY));

        MenuItem repeatYearly = new MenuItem("Yearly");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.YEARLY));

        MenuItem doNotRepeat = new MenuItem("Do not repeat");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.NONE));


        repeatMenuButton.getItems().addAll(doNotRepeat, repeatDaily, repeatWeekly, repeatMonthly, repeatYearly);
    }

    private void setTaskRepetition(Task task, RepeatTask repetition) {
        if (task == null) return;

        task.setRepetition(repetition);
    }

    private void initializeRemindMeMenuButton() {

        MenuItem remindMeLaterToday = new MenuItem("Later today");
        MenuItem remindMeTomorrow = new MenuItem("Tomorrow");
        MenuItem remindMeNextWeek = new MenuItem("Next Week");
        MenuItem remindMeCustomTime = new MenuItem("Custom");

        // todo: figure out how to save that | think about custom time
        remindMeLaterToday.setOnAction(e -> setReminder(7200000)); // 2 hours
        remindMeTomorrow.setOnAction(e -> setReminder(LocalDate.now().plusDays(1))); // 1 day
        remindMeNextWeek.setOnAction(e -> setReminder(LocalDate.now().plusDays(7))); // 1 week
        remindMeCustomTime.setOnAction(e -> Platform.runLater(() -> CustomTimePopupController.display(getSelectedTask())));


        remindMeMenuButton.getItems().addAll(remindMeLaterToday, remindMeTomorrow, remindMeNextWeek, remindMeCustomTime);
    }

    private void setReminder(long delay) {

        Task task = getSelectedTask();

        if (task == null) return;

        Timer timer = new Timer();
        TimerTask job = getTimerTask(task);

        timer.schedule(job, delay);
    }

    private void setReminder(LocalDate date) {

        Task task = getSelectedTask();

        if (task == null) return;

        Timer timer = new Timer();
        TimerTask job = getTimerTask(task);

        timer.schedule(job, Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private TimerTask getTimerTask(Task task) {
        return new TimerTask() {
            @Override
            public void run() {
                // play remind jingle
                playReminderJingle();

                // todo: set off alert box with task info
                Platform.runLater(() -> ReminderPopupController.display(task));
            }
        };
    }

    private TaskListCellController getCustomTaskListCellController() {
        TaskListCellController taskListCell = TaskListCellController.newInstance();

        if (taskListCell == null)
            throw new RuntimeException("TaskListCellController object is a null!");

        // set reference of this object in task list controller, so it can access methods of this object
        taskListCell.setOrganizerControllerReference(this);
        return taskListCell;
    }

    private void deleteSelectedTaskAndRefresh() {
        if (getSelectedTask() == null) return;
        getSelectedCategoryItem().getTasks().remove(getSelectedTask());
        refreshTaskList();
    }

    private void deleteSelectedTaskFromCompletedTasksAndRefresh() {
        if (getSelectedTaskFromCompletedTasksList() == null) return;

        completedTasksListView.getItems().remove(getSelectedTaskFromCompletedTasksList());
        completedTasksListView.refresh();
    }

    private void playReminderJingle() {
       try {
           playSound(getFile("src/main/resources/jingels/alarm.mp3"));
       } catch (MalformedURLException e) {
           throw new RuntimeException(e);
       }
    }

    private void playDoneJingle() {
        try {
            playSound(getFile("src/main/resources/jingels/done.mp3"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String pathname) {
        return new File(pathname);
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

    private String[] getListOfAvailableBackgrounds(String path) {
        File file = getFile(path);
        return file.list();
    }

    private void loadBackgroundsFromDirectory() {

        // get all backgrounds from background folder
        String pathToBGFolder = "src/main/resources/backgrounds/";
        String[] backgrounds = getListOfAvailableBackgrounds(pathToBGFolder);

        // add newly-found backgrounds to the menu button as an item
        for (var image : backgrounds) {

            MenuItem item = new MenuItem(image);
            item.setOnAction(event -> setBackground(pathToBGFolder, image));
            backgroundMenuButton.getItems().add(item);
        }

    }

    private void setBackground(String pathToBGFolder, String image) {
        try {
            Background background = createBackgroundForHBox(new Image(new FileInputStream(pathToBGFolder + image)));
            backgroundHBox.setBackground(background);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Background createBackgroundForHBox(Image image) {
        BackgroundSize size = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);

        return new Background(new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                size));
    }

    private void loadAndRefreshCategories() {
        categories = new ArrayList<>();
        createAndLoadSampleData(); // todo get rid of that at some point
        refreshCategories();
    }

    private void updateTaskDescription() {
        String content = taskDescriptionTextArea.getText();
        Task task = getSelectedTask();

        if (content == null || content.isBlank() || task == null) return;

        taskDescriptionTextArea.setText(task.getDescription());

        getSelectedTask().setDescription(content);
        refreshTaskDetails();
    }

    private void refreshCategories () {
        categoriesListView.setItems(FXCollections.observableArrayList(categories));
    }

    private void refreshTaskList () {
        int selectedCategoryIndex = getSelectedCategoryIndex();
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size()) return;

        TaskList tasks = categories.get(selectedCategoryIndex);

        activeTasksListView.setItems(FXCollections.observableArrayList(tasks.getTasks()));
        setContentOfCategoryLabel();
    }

    private void refreshTaskDetails () {
        if (getSelectedTask() == null) return;
        setContentOfDescriptionTextAreaInTaskDetailsPane();
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
        Task task = getSelectedTask();

        String title = task.getTitle() == null ? "" : task.getTitle();

        taskTitleLabel.setText(title);
    }

    private void setContentOfDescriptionTextAreaInTaskDetailsPane() {

        String description = getSelectedTask().getDescription();

        if (description != null)
            taskDescriptionTextArea.setText(description);

    }

    private int getSelectedCategoryIndex () {
        return categoriesListView.getSelectionModel().getSelectedIndex();
    }

    private TaskList getSelectedCategoryItem () {
        return categoriesListView.getSelectionModel().getSelectedItem();
    }

    private Task getSelectedTask () {
        return activeTasksListView.getSelectionModel().getSelectedItem();
    }

    private Task getSelectedTaskFromCompletedTasksList () {
        return completedTasksListView.getSelectionModel().getSelectedItem();
    }

    private void clearAddTaskTextFiled() {
        addTaskTextField.setText("");
    }

    private void addTaskToTaskList(Task task) {
        getSelectedCategoryItem().addTask(task);
    }

    private void disableButtonsIfSelectedTaskIsNull() {
        disableTaskRelatedButtonsAndMenus(getSelectedTask() == null);
    }

    // -------------------------> test/ debug methods

    private void createAndLoadSampleData() {

        // create some sample tasks
        Task taskA = new Task();
        taskA.setTitle("Do something...");

        Task taskB = new Task();
        taskB.setTitle("Do that other thing that you had to do yesterday...");

        Task taskC = new Task();
        taskC.setTitle("Do anything you want to do...");


        // create sample taskList objects
        TaskList taskList1 = new TaskList("in");
        taskList1.addTask(taskA);

        TaskList taskList2 = new TaskList("important");
        taskList2.addTask(taskB);

        TaskList taskList3 = new TaskList("Planned");
        taskList3.addTask(taskC);

        // add sample taskList objects to categories object
        categories.add(taskList1);
        categories.add(taskList2);
        categories.add(taskList3);

    }

}
