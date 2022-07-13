package com.omicron.organizerb.controller;

import com.omicron.organizerb.Main;
import com.omicron.organizerb.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrganizerController implements Initializable {


    // ========================================================================================
    // Fields
    // ========================================================================================

    // -------------------------> FXML components

    @FXML
    public HBox backgroundHBox;

    @FXML
    public Label taskListLabel;

    @FXML
    public Label taskTitleLabel;

    @FXML
    public ListView<Task> activeTasksListView;

    @FXML
    public ListView<Task> completedTasksListView;

    @FXML
    public ListView<TaskList> categoriesListView;

    @FXML
    public ContextMenu taskListContextMenu;

    @FXML
    public ContextMenu categoryContextMenu;

    @FXML
    public TextArea taskDescriptionTextArea;

    @FXML
    public TextField addTaskTextField;

    @FXML
    public TextField addCategoryTextField;

    @FXML
    public DatePicker addDueDatePicker;

    @FXML
    public Button deleteButton;

    @FXML
    public Button markAsDoneButton;

    @FXML
    public MenuButton remindMeMenuButton;

    @FXML
    public MenuButton repeatMenuButton;

    @FXML
    public MenuButton settingsMenuButton;


    // -------------------------> private fields

    private ArrayList<TaskList> categories;

    private ApplicationSettings applicationSettings;

    private static final Logger logger = Logger.getLogger(OrganizerController.class.getName());


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Overridden methods
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loadAndRefreshCategories();
        loadApplicationSettings();
        loadIconsForButtons();
        loadAndSelectCategoryOnStartup();

        setCustomCellsFactoriesForTaskLists();
        setCustomCellsFactoriesForCategories();

        initializeCategoryContextMenu();
        initializeTaskListContextMenu();
        initializeRepeatMenuButton();
        initializeRemindMeMenuButton();
        initializeSettingsMenuButton();

        disableTaskRelatedButtonsAndMenus(true);

        checkForRepeatingTasks();

        setOnCloseAction();
    }


    // -------------------------> FXML methods

    @FXML
    public void loadTasksEventHandler() {
        loadTasks();
    }

    @FXML
    public void addTaskEventHandler(KeyEvent event) {
        handleAddingNewTask(event);
    }

    @FXML
    public void addCategoryEventHandler(KeyEvent event) {
        handleAddingNewCategory(event);
    }

    @FXML
    public void loadTaskDetailsEventHandler() {
        loadActiveTaskDetails();
    }

    @FXML
    public void loadCompletedTaskDetailsEventHandler() {
        loadCompletedTaskDetails();
    }

    @FXML
    public void updateTaskDescriptionEventHandler() {
        updateTaskDescription(getSelectedTask());
    }

    @FXML
    public void addTaskDueDateEventHandler() {
        setSelectedTaskDeadLine(getSelectedTask());
    }

    @FXML
    public void deleteTaskEventHandler() {
        deleteSelectedTaskAndRefresh(getSelectedTask());
    }

    @FXML
    public void markTaskAsDoneOrActiveEventHandler() {
        markTaskAsDoneOrActive();
    }


    // -------------------------> internal methods

    private void markTaskAsDoneOrActive() {
        Task task = getSelectedTask();
        if (completedTasksListView.getItems().contains(task))
            markTaskAsActiveAndAddToActiveList(task);
        else
            markTaskAsDoneAndAddToCompletedList(task);
    }

    private void loadActiveTaskDetails() {
        completedTasksListView.getSelectionModel().clearSelection();
        loadTaskDetails(getSelectedTask());
    }

    private void loadCompletedTaskDetails() {
        activeTasksListView.getSelectionModel().clearSelection();
        Task task = completedTasksListView.getSelectionModel().getSelectedItem();
        loadTaskDetails(task);
    }

    private void setOnCloseAction() {
        Main.mainStageReference.setOnCloseRequest(e -> onClose());
    }

    private void loadTasks() {
        disableTaskRelatedButtonsAndMenus(true);
        refreshTaskList();
    }

    private void checkForRepeatingTasks() {
        ArrayList<Task> tasksToRemove = new ArrayList<>();

        for (var completedTask : completedTasksListView.getItems()) {
            Task value = loadTaskIfRepeated(completedTask);
            if (value != null)
                tasksToRemove.add(completedTask);
        }
        for (var task : tasksToRemove) {
            completedTasksListView.getItems().remove(task);
        }
    }

    private void loadAndSelectCategoryOnStartup() {
        categoriesListView.getSelectionModel().select(applicationSettings.getLastSelectedTaskListIndex());
        activeTasksListView.getSelectionModel().select(0);
        refreshTaskList();
    }

    private void loadApplicationTheme(String path) {
        try {
            String theme = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
            backgroundHBox.getStylesheets().remove(0);
            backgroundHBox.getStylesheets().add(theme);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Application themes could not be loaded! ");
            e.printStackTrace();
        }
    }

    private void loadApplicationSettings() {
        try {
            applicationSettings = deserializeApplicationSettings();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Settings file not found. ");
            e.printStackTrace();
            applicationSettings = new ApplicationSettings();
        }

        if (applicationSettings.getBackground() != null)
            setHBoxBackground("backgrounds/", applicationSettings.getBackground());

        loadApplicationTheme(applicationSettings.getApplicationThemeCSS());
    }

    private void loadTaskDetails(Task task) {

        if (completedTasksListView.getItems().contains(task))
            markAsDoneButton.setText("Mark as Active");
        else
            markAsDoneButton.setText("Mark as Done");

        disableButtonsIfSelectedTaskIsNull();
        refreshTaskDetails(task);
    }

    private void disableTaskRelatedButtonsAndMenus(boolean value) {
        deleteButton.disableProperty().setValue(value);
        remindMeMenuButton.disableProperty().setValue(value);
        repeatMenuButton.disableProperty().setValue(value);
        markAsDoneButton.disableProperty().setValue(value);
        addDueDatePicker.disableProperty().setValue(value);
        taskDescriptionTextArea.disableProperty().setValue(value);
    }

    private Task loadTaskIfRepeated(Task task) {

        if (task == null) return null;
        if (task.getRepetition() == RepeatTask.NONE) return null;

        if (Objects.equals(task.getDayOfRepetition(), LocalDate.now())) {

            task.setDone(false);
            task.setRepetition(task.getRepetition());

            categoriesListView.getItems().get(0).addTask(task);

            refreshCategories();
            refreshTaskList();
            return task;
        }
        return null;
    }

    private void markTaskAsDoneAndAddToCompletedList(Task task) {

        task.setDone(true);
        getSelectedCategoryItem().getTasks().remove(task);
        completedTasksListView.getItems().add(task);

        playDoneJingle();
        refreshTaskList();

    }

    private void markTaskAsActiveAndAddToActiveList(Task task) {

        completedTasksListView.getItems().remove(task);
        task.setDone(false);
        getSelectedCategoryItem().getTasks().add(task);

        refreshTaskList();
    }

    private void setSelectedTaskDeadLine(Task task) {
        if (addDueDatePicker.getValue() != null)
            task.setDate(addDueDatePicker.getValue());
    }

    private void handleAddingNewTask(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!(addTaskTextField.getText() == null || addTaskTextField.getText().isBlank())) {
                createAndAddNewTaskToSelectedCategory();
            }
        }
    }

    private void createAndAddNewTaskToSelectedCategory() {
        Task task = new Task();
        task.setTitle(addTaskTextField.getText());

        addTaskToTaskList(task);
        refreshTaskList();
        addTaskTextField.clear();
    }

    private void handleAddingNewCategory(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            boolean isCategoryNameValid =
                    addCategoryTextField.getText() != null && !addCategoryTextField.getText().isBlank();

            if (isCategoryNameValid) {
                createAndAddNewCategory();
            }
        }
    }

    private void createAndAddNewCategory() {
        TaskList taskList = new TaskList();

        taskList.setTaskListTitle(addCategoryTextField.getText());
        categories.add(taskList);
        refreshCategories();
        refreshMoveTaskMenu();

        addCategoryTextField.setText("");
    }

    private void setCustomCellsFactoriesForTaskLists() {
        activeTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
        completedTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
    }

    private void setCustomCellsFactoriesForCategories() {
        categoriesListView.setCellFactory(lv -> getCustomCategoryListCellController());
    }

    private void loadIconsForButtons() {
        setIconForNodes(markAsDoneButton, "/icons/done.png");
        setIconForNodes(deleteButton, "/icons/delete.png");
        setIconForNodes(remindMeMenuButton, "/icons/remind.png");
        setIconForNodes(repeatMenuButton, "/icons/repeat.png");
        setIconForNodes(settingsMenuButton, "/icons/settings.png");
    }

    private void setIconForNodes(ButtonBase buttonBase, String pathToIcon) {
        try {
            buttonBase.setGraphic(Utility.getIcon(pathToIcon));
            buttonBase.alignmentProperty().setValue(Pos.BOTTOM_LEFT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Node icons could not be set. ");
            e.printStackTrace();
        }
    }


    private void initializeTaskListContextMenu() {

        MenuItem markTaskAsDone = initializeMarkTaskAsDoneMenuItem();
        Menu moveTask = initializeMoveTaskToMenu();
        MenuItem deleteTask = initializeDeleteTaskMenuItem();
        Menu setTaskPriority = initializeTaskPriorityMenu();

        taskListContextMenu.getItems().addAll(markTaskAsDone, moveTask, deleteTask, setTaskPriority);
    }

    // todo think of better way of doing that
    void refreshMoveTaskMenu() {
        taskListContextMenu.getItems().set(1, initializeMoveTaskToMenu());
    }

    private Menu initializeTaskPriorityMenu() {
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
        markTaskAsDone.setOnAction(event -> markTaskAsDoneOrActiveEventHandler());
        return markTaskAsDone;
    }

    private Menu initializeMoveTaskToMenu() {
        Menu moveTask = new Menu("Move task to...");

        for (int i = 0; i < categoriesListView.getItems().size(); i++) {

            TaskList category = categoriesListView.getItems().get(i);

            MenuItem menuItem = new MenuItem(category.getTaskListTitle());

            moveTaskToDifferentCategory(category, menuItem);

            moveTask.getItems().add(menuItem);

        }
        return moveTask;
    }


    private void moveTaskToDifferentCategory(TaskList category, MenuItem menuItem) {
        menuItem.setOnAction(e -> {
            Task task = getSelectedTask();
            if (task == null) return;

            getSelectedCategoryItem().getTasks().remove(task);
            category.addTask(task);
            refreshTaskList();
            disableTaskRelatedButtonsAndMenus(true);
            refreshMoveTaskMenu();
        });
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
        refreshMoveTaskMenu();
    }

    private void setPriorityForSelectedTask(TaskPriority priority) {
        Task task = getSelectedTask();
        if (task == null) return;

        task.setPriority(priority);
        refreshTaskList();
    }

    private void initializeRepeatMenuButton() {
        MenuItem repeatDaily = new MenuItem("Daily");
        repeatDaily.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.DAILY));

        MenuItem repeatWeekly = new MenuItem("Weekly");
        repeatWeekly.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.WEEKLY));

        MenuItem repeatMonthly = new MenuItem("Monthly");
        repeatMonthly.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.MONTHLY));

        MenuItem repeatYearly = new MenuItem("Yearly");
        repeatYearly.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.YEARLY));

        MenuItem doNotRepeat = new MenuItem("Do not repeat");
        doNotRepeat.setOnAction(e -> setTaskRepetition(getSelectedTask(), RepeatTask.NONE));

        repeatMenuButton.getItems().addAll(doNotRepeat, repeatDaily, repeatWeekly, repeatMonthly, repeatYearly);
    }

    private void initializeSettingsMenuButton() {

        CheckMenuItem switchTheme = new CheckMenuItem("Toggle theme");
        switchTheme.setOnAction(e -> switchApplicationTheme(switchTheme));

        Menu backgroundMenu = loadBackgroundsIntoMenu();
        settingsMenuButton.getItems().addAll(switchTheme, backgroundMenu);

    }

    private Menu loadBackgroundsIntoMenu() {
        String pathToBGFolder = "backgrounds/";

        String[] backgrounds = Utility.getAllFilesInDirectory(pathToBGFolder);

        Menu backgroundMenu = addAvailableBackgroundsToMenu(pathToBGFolder, backgrounds);
        backgroundMenu.setText("Backgrounds");
        return backgroundMenu;
    }

    private void switchApplicationTheme(CheckMenuItem toggleTheme) {
        try {
            if (toggleTheme.isSelected()) {
                loadApplicationTheme("/css/organizerLight.css");
                applicationSettings.setApplicationThemeCSS("/css/organizerLight.css");
                return;
            }

            loadApplicationTheme("/css/organizerDark.css");
            applicationSettings.setApplicationThemeCSS("/css/organizerDark.css");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Application theme could not be switched! ");
            e.printStackTrace();
        }
    }


    private void setTaskRepetition(Task task, RepeatTask repetition) {
        if (task == null) return;

        task.setRepetition(repetition);
    }

    private void initializeRemindMeMenuButton() {

        MenuItem remindMeLaterToday = new MenuItem("Later today");
        MenuItem remindMeTomorrow = new MenuItem("Tomorrow");
        MenuItem remindMeNextWeek = new MenuItem("Next Week");
        MenuItem customTime = new MenuItem("Custom");

        BiConsumer<LocalTime, LocalDate> consumer = (LocalTime time, LocalDate date) -> {
            Task task = getSelectedTask();
            task.setDate(date);
            task.setTime(time);
            setReminder(task);
        };

        remindMeLaterToday.setOnAction(e ->
                consumer.accept(LocalTime.now().plusHours(1), LocalDate.now()));

        remindMeTomorrow.setOnAction(e ->
                consumer.accept(LocalTime.of(0, 0), LocalDate.now().plusDays(1)));

        remindMeNextWeek.setOnAction(e ->
                consumer.accept(LocalTime.of(0, 0), LocalDate.now().plusDays(7)));

        customTime.setOnAction(e -> Platform.runLater(() -> {
            Task task = getSelectedTask();

            try {

                createAndShowCustomTimePopup(task);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }


        }));

        remindMeMenuButton.getItems().addAll(remindMeLaterToday, remindMeTomorrow, remindMeNextWeek, customTime);
    }

    private void createAndShowCustomTimePopup(Task task) throws IOException {
        FXMLLoader loader = Utility.getFXMLLoader("fxml/timeAndDatePopup.fxml");
        Stage popupStage = new Stage();
        TimeAndDateController popupController =  new TimeAndDateController(popupStage, task);

        popupController.setOrganizerControllerReference(this);

        loader.setControllerFactory(event -> popupController);
        Scene scene = new Scene(loader.load());
        popupStage.setScene(scene);

        popupStage.showAndWait();
    }

    void setReminder(Task task) {

        if (task == null) return;

        Timer timer = new Timer();
        TimerTask job = createTimerTaskForPlayingReminderJingle(task);

        if (task.getDate().equals(LocalDate.now())) {

            long differenceInMilliseconds = task.getTimeDifferenceInMilliseconds(LocalTime.now());

            timer.schedule(job, differenceInMilliseconds);
            return;
        }

        timer.schedule(job, Date.from(task.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private TimerTask createTimerTaskForPlayingReminderJingle(Task task) {
        return new TimerTask() {
            @Override
            public void run() {
                playReminderJingle();
                Platform.runLater(() -> ReminderPopupController.display(task));
            }
        };
    }

    // todo can be generified? eg via interface
    private TaskListCellController getCustomTaskListCellController() {
        TaskListCellController taskListCell = TaskListCellController.newInstance();

        if (taskListCell == null)
            throw new RuntimeException("TaskListCellController object is a null!");

        // set reference of this object in task list controller, so it can access methods of this object
        taskListCell.setOrganizerControllerReference(this);
        return taskListCell;
    }

    private CategoryListCellController getCustomCategoryListCellController() {
        CategoryListCellController categoryListCell = CategoryListCellController.newInstance();

        if (categoryListCell == null)
            throw new RuntimeException("CategoryListCellController object is a null!");

        categoryListCell.setOrganizerControllerReference(this);
        return categoryListCell;
    }

    private void deleteSelectedTaskAndRefresh(Task task) {
        getSelectedCategoryItem().getTasks().remove(task);
        completedTasksListView.getItems().remove(task);
        refreshTaskList();
        disableTaskRelatedButtonsAndMenus(true);
    }

    private void playReminderJingle() {
        try {
            Utility.playSound(Utility.getFile("jingle/alarm.wav"));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Sound could not be played. ");
            e.printStackTrace();
        }
    }

    private void playDoneJingle() {
        try {
            Utility.playSound(Utility.getFile("jingle/done.wav"));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Sound could not be played. ");
            e.printStackTrace();
        }
    }


    private Menu addAvailableBackgroundsToMenu(String pathToBGFolder, String[] backgrounds) {
        Menu backgroundsMenu = new Menu();

        for (var image : backgrounds) {

            MenuItem item = new MenuItem(image);
            item.setOnAction(event -> setHBoxBackground(pathToBGFolder, image));
            backgroundsMenu.getItems().add(item);
        }
        return backgroundsMenu;
    }

    private void setHBoxBackground(String pathToBGFolder, String image) {
        try {
            if (applicationSettings != null)
                applicationSettings.setBackground(image);

            Background background = createBackgroundFromImage(new Image(new FileInputStream(pathToBGFolder + image)));
            backgroundHBox.setBackground(background);

        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Background could not be set. ");
            e.printStackTrace();
        }
    }

    private Background createBackgroundFromImage(Image image) {
        BackgroundSize size = new BackgroundSize(
                BackgroundSize.AUTO,
                BackgroundSize.AUTO,
                true,
                true,
                true,
                false);

        return new Background(new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                size));
    }

    private void loadAndRefreshCategories() {
        try {
            loadDeserializedCategories();
        } catch (Exception e) {
            loadSampleCategories();
            logger.log(Level.WARNING, "Categories could not be loaded! ");
            e.printStackTrace();
        } finally {
            refreshCategories();
        }
    }

    private void loadSampleCategories() {
        categories = new ArrayList<>();
        createAndLoadSampleData();
    }

    private void loadDeserializedCategories() {
        categories = deserializeCategories();
        completedTasksListView.setItems(FXCollections.observableArrayList(deserializeCompletedTasks()));
    }

    private void updateTaskDescription(Task task) {
        String content = taskDescriptionTextArea.getText();

        if (content == null || content.isBlank() || task == null) return;

        taskDescriptionTextArea.setText(task.getDescription());

        getSelectedTask().setDescription(content);
        refreshTaskDetails(task);
    }

    private void refreshCategories() {
        categoriesListView.refresh();
        categoriesListView.setItems(FXCollections.observableArrayList(categories));

    }

    private void refreshTaskList() {
        int selectedCategoryIndex = getSelectedCategoryIndex();

        if (!isCategoryIndexWithinRange(selectedCategoryIndex))
            return;

        TaskList tasks = categories.get(selectedCategoryIndex);

        activeTasksListView.setItems(FXCollections.observableArrayList(tasks.getTasks()));
        completedTasksListView.refresh();
        updateContentOfCategoryLabel();
    }

    private boolean isCategoryIndexWithinRange(int selectedCategoryIndex) {
        return !(selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size());
    }

    private void refreshTaskDetails(Task task) {
        if (task == null) return;
        updateTaskDescriptionInDetailsPane(task);
        updateContentOfTaskTitleLabel(task);
        updateContentOfDatePicker(task);
    }

    private void updateContentOfDatePicker(Task task) {
        addDueDatePicker.setValue(task.getDate());
    }

    private void updateContentOfCategoryLabel() {
        taskListLabel.setText(getSelectedCategoryItem().getTaskListTitle());
    }

    private void updateContentOfTaskTitleLabel(Task task) {

        String title = task.getTitle() == null ? "" : task.getTitle();

        taskTitleLabel.setText(title);
    }

    private void updateTaskDescriptionInDetailsPane(Task task) {

        String description = task.getDescription();

        if (description != null)
            taskDescriptionTextArea.setText(description);

    }

    private int getSelectedCategoryIndex() {
        return categoriesListView.getSelectionModel().getSelectedIndex();
    }

    private TaskList getSelectedCategoryItem() {
        applicationSettings.setLastSelectedTaskListIndex(categoriesListView.getSelectionModel().getSelectedIndex());
        return categoriesListView.getSelectionModel().getSelectedItem();
    }

    private Task getSelectedTask() {
        Task activeTask = activeTasksListView.getSelectionModel().getSelectedItem();
        Task completedTask = completedTasksListView.getSelectionModel().getSelectedItem();

        return activeTask == null ? completedTask : activeTask;
    }


    private void addTaskToTaskList(Task task) {
        getSelectedCategoryItem().addTask(task);
    }

    private void disableButtonsIfSelectedTaskIsNull() {
        disableTaskRelatedButtonsAndMenus(getSelectedTask() == null);
    }

    private void saveDataToDisk() {
        serializeCategories();
        serializeCompletedTasks();
        serializeApplicationSettings();
    }

    private void serializeCategories() {
        String path = "programData/categories";
        ArrayList<TaskList> categories = new ArrayList<>(categoriesListView.getItems());
        Utility.serializeObject(categories, path);
    }

    private void serializeCompletedTasks() {
        String path = "programData/completed";
        ArrayList<Task> completedTasks = new ArrayList<>(completedTasksListView.getItems());
        Utility.serializeObject(completedTasks, path);
    }

    private void serializeApplicationSettings() {
        String path = "programData/settings";
        Utility.serializeObject(this.applicationSettings, path);
    }

    private ApplicationSettings deserializeApplicationSettings() {
        String path = "programData/settings";
        return (ApplicationSettings) Utility.deserializeObject(path);
    }


    @SuppressWarnings("unchecked")
    private ArrayList<TaskList> deserializeCategories() {
        return (ArrayList<TaskList>) Utility.deserializeObject("programData/categories");
    }


    @SuppressWarnings("unchecked")
    private ArrayList<Task> deserializeCompletedTasks() {
        return (ArrayList<Task>) Utility.deserializeObject("programData/completed");
    }


    private void onClose() {
        saveDataToDisk();
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

        TaskList taskList3 = new TaskList("planned");
        taskList3.addTask(taskC);

        TaskList taskList4 = new TaskList("waiting");

        TaskList taskList5 = new TaskList("ongoing");

        // add sample taskList objects to categories object
        categories.add(taskList1);
        categories.add(taskList2);
        categories.add(taskList3);
        categories.add(taskList4);
        categories.add(taskList5);

    }

}
