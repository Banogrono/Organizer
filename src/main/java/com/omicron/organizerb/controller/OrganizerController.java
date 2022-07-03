package com.omicron.organizerb.controller;

import com.omicron.organizerb.Main;
import com.omicron.organizerb.model.*;
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiConsumer;

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
    public MenuButton backgroundMenuButton;

    @FXML
    public MenuButton remindMeMenuButton;

    @FXML
    public MenuButton repeatMenuButton;

    @FXML
    public ToggleButton themeToggleButton;


    // -------------------------> private fields

    private ArrayList<TaskList> categories;

    private ApplicationSettings applicationSettings;


    // ========================================================================================
    // Methods
    // ========================================================================================

    // -------------------------> Overridden methods
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        loadAndRefreshCategories();
        loadBackgroundsFromDirectory();
        loadApplicationSettings();
        loadIconsForButtons();
        loadAndSelectCategoryOnStartup();

        setCustomCellsFactoriesForTaskLists();

        initializeCategoryContextMenu();
        initializeTaskListContextMenu();
        initializeRepeatMenuButton();
        initializeRemindMeMenuButton();

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
        addNewTaskToSelectedCategory(event);
    }

    @FXML
    public void addCategoryEventHandler(KeyEvent event) {
        addNewCategory(event);
    }

    @FXML
    public void loadTaskDetailsEventHandler() {
        loadTaskDetails();
    }

    @FXML
    public void updateTaskDescriptionEventHandler() {
        updateTaskDescription();
    }

    @FXML
    public void addTaskDueDateEventHandler() {
        setSelectedTaskDeadLine();
    }

    @FXML
    public void deleteTaskEventHandler() {
        deleteSelectedTaskAndRefresh();
    }

    @FXML
    public void markTaskAsDoneEventHandler() {
        markTaskAsDoneAndAddToCompletedList();
    }

    @FXML
    public void switchThemeEventHandler() {
        switchApplicationTheme();
    }

    // -------------------------> internal methods

    private void setOnCloseAction() {
        Main.mainStageReference.setOnCloseRequest(e -> onClose());
    }

    private void loadTasks() {
        disableTaskRelatedButtonsAndMenus(true);
        refreshTaskList();
    }

    private void switchApplicationTheme() {
        try {
            if (themeToggleButton.isSelected()) {
                loadApplicationTheme("/css/organizerLight.css");
                applicationSettings.setApplicationThemeCSS("/css/organizerLight.css");
                return;
            }

            loadApplicationTheme("/css/organizerDark.css");
            applicationSettings.setApplicationThemeCSS("/css/organizerDark.css");

        } catch (Exception e) {
            throw new RuntimeException("Application theme could not be switched! " + e);
        }
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

            if (backgroundHBox.getStylesheets().get(0).contains("Light"))
                themeToggleButton.setGraphic(getIcon("/icons/light_off.png"));
            else
                themeToggleButton.setGraphic(getIcon("/icons/light_on.png"));
        } catch (Exception e) {
            throw new RuntimeException("Application themes could not be loaded! " + e);
        }
    }

    private void loadApplicationSettings() {
        try {
            applicationSettings = deserializeApplicationSettings();
        } catch (Exception e) {
            System.out.println("Settings file not found. " + e);
            applicationSettings = new ApplicationSettings();
        }

        if (applicationSettings.getBackground() != null)
            setHBoxBackground("backgrounds/", applicationSettings.getBackground());

        loadApplicationTheme(applicationSettings.getApplicationThemeCSS());
    }

    private void loadTaskDetails() {
        disableButtonsIfSelectedTaskIsNull();
        refreshTaskDetails();
    }

    private void disableTaskRelatedButtonsAndMenus(boolean value) {
        deleteButton.disableProperty().setValue(value);
        remindMeMenuButton.disableProperty().setValue(value);
        repeatMenuButton.disableProperty().setValue(value);
        markAsDoneButton.disableProperty().setValue(value);
        addDueDatePicker.disableProperty().setValue(value);
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

    private void markTaskAsDoneAndAddToCompletedList() {
        Task task = getSelectedTask();
        if (task == null) return;

        // todo: the mark as done option for tasks in completed list should be grayed out or turned into 'mark as active'
        if (completedTasksListView.getItems().contains(task))
            return;

        task.setDone(true);
        getSelectedCategoryItem().getTasks().remove(task);
        completedTasksListView.getItems().add(task);

        playDoneJingle();
        refreshTaskList();

    }

    private void setSelectedTaskDeadLine() {
        if (addDueDatePicker.getValue() != null)
            setSelectedTaskDeadLine(addDueDatePicker.getValue());
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

            boolean isCategoryNameValid =
                    addCategoryTextField.getText() != null && !addCategoryTextField.getText().isBlank();

            if (isCategoryNameValid) {

                TaskList taskList = new TaskList();

                taskList.setTaskListTitle(addCategoryTextField.getText());
                categories.add(taskList);
                refreshCategories();

                addCategoryTextField.setText("");
            }
        }
    }

    private void setCustomCellsFactoriesForTaskLists() {
        activeTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
        completedTasksListView.setCellFactory(lv -> getCustomTaskListCellController());
    }

    private void loadIconsForButtons() {

        setIconForNodes(markAsDoneButton, "/icons/done.png");
        setIconForNodes(deleteButton, "/icons/delete.png");
        setIconForNodes(remindMeMenuButton, "/icons/remind.png");
        setIconForNodes(repeatMenuButton, "/icons/repeat.png");
        setIconForNodes(backgroundMenuButton, "/icons/background.png");
    }

    private void setIconForNodes(ButtonBase buttonBase, String pathToIcon) {
        try {
            buttonBase.setGraphic(getIcon(pathToIcon));
            buttonBase.alignmentProperty().setValue(Pos.BOTTOM_LEFT);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private ImageView getIcon(String path) throws MalformedURLException {
        String imageLocation = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();

        ImageView img = new ImageView(new Image(imageLocation));
        int MAX_ICON_SIZE = 24;
        img.fitWidthProperty().setValue(MAX_ICON_SIZE);
        img.fitHeightProperty().setValue(MAX_ICON_SIZE);

        return img;
    }

    private void initializeTaskListContextMenu() {

        MenuItem markTaskAsDone = initializeMarkTaskAsDoneMenuItem();
        Menu moveTask = initializeMoveTaskToMenu();
        MenuItem deleteTask = initializeDeleteTaskMenuItem();
        Menu setTaskPriority = initializeTaskPriorityMenu();

        taskListContextMenu.getItems().addAll(markTaskAsDone, moveTask, deleteTask, setTaskPriority);
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
        markTaskAsDone.setOnAction(event -> markTaskAsDoneEventHandler());
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
        });
    }

    private void initializeCategoryContextMenu() {

        MenuItem renameCategoryMenuItem = new MenuItem("Rename category");
        renameCategoryMenuItem.setOnAction(event -> renameCategory());

        MenuItem deleteCategoryMenuItem = new MenuItem("Delete category");
        deleteCategoryMenuItem.setOnAction(event -> deleteCategory());

        categoryContextMenu.getItems().add(renameCategoryMenuItem);
        categoryContextMenu.getItems().add(deleteCategoryMenuItem);
    }

    // todo: make that work as it should
    private void renameCategory() {
        if (getSelectedCategoryItem() == null) return;
        getSelectedCategoryItem().setTaskListTitle("next");
        refreshCategories();
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

    private void setTaskRepetition(Task task, RepeatTask repetition) {
        if (task == null) return;

        task.setRepetition(repetition);
    }

    private void initializeRemindMeMenuButton() {

        MenuItem remindMeLaterToday = new MenuItem("Later today");
        MenuItem remindMeTomorrow = new MenuItem("Tomorrow");
        MenuItem remindMeNextWeek = new MenuItem("Next Week");
        MenuItem remindMeCustomTime = new MenuItem("Custom");

        BiConsumer<LocalTime, LocalDate> consumer = (LocalTime time, LocalDate date) -> {
            Task task = getSelectedTask();
            task.setDate(date);
            task.setTime(time);
            setReminder(task);
        };

        remindMeLaterToday.setOnAction(e ->
                consumer.accept(LocalTime.now().plusHours(1), LocalDate.now()));

        remindMeTomorrow.setOnAction(e ->
                consumer.accept(LocalTime.of(0,0), LocalDate.now().plusDays(1)));

        remindMeNextWeek.setOnAction(e ->
                consumer.accept(LocalTime.of(0,0), LocalDate.now().plusDays(7)));

        remindMeCustomTime.setOnAction(e -> Platform.runLater(() -> {
            Task task = getSelectedTask();
            CustomTimePopupController.display(task);
            setReminder(task);
        }));

        remindMeMenuButton.getItems().addAll(remindMeLaterToday, remindMeTomorrow, remindMeNextWeek, remindMeCustomTime);
    }

    private void setReminder(Task task) {

        if (task == null) return;

        Timer timer = new Timer();
        TimerTask job = getTimerTask(task);

        if (task.getDate().equals(LocalDate.now())) {

            long differenceInMilliseconds = CompareAndReturnTimeDifferenceInMilliseconds(task);

            timer.schedule(job, differenceInMilliseconds);
            return;
        }

       timer.schedule(job, Date.from(task.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private long CompareAndReturnTimeDifferenceInMilliseconds(Task task) {
        LocalTime now = LocalTime.now();
        LocalTime taskTime = task.getTime();

        return (taskTime
                .minusHours(now.getHour())
                .minusMinutes(now.getMinute())
                .minusSeconds(now.getSecond())
                .toSecondOfDay()) * 1000L;
    }

    private TimerTask getTimerTask(Task task) {
        return new TimerTask() {
            @Override
            public void run() {
                // play remind jingle
                playReminderJingle();

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
        completedTasksListView.getItems().remove(getSelectedTask());

        refreshTaskList();
    }

    private void playReminderJingle() {
        try {
            playSound(getFile("jingle/alarm.mp3"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void playDoneJingle() {
        try {
            playSound(getFile("jingle/done.mp3"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String pathname) {
        return new File(pathname);
    }

    // todo: separate thread for that?
    private void playSound(File soundFile) throws MalformedURLException {
        Media sound = new Media(soundFile.toURI().toURL().toString());

        MediaPlayer mediaPlayer = new MediaPlayer(sound);

        mediaPlayer.play();
    }

    private void setSelectedTaskDeadLine(LocalDate date) {
        if (getSelectedTask() == null) return;

        getSelectedTask().setDate(date);
    }

    private String[] findBackgrounds(String path) {
        File file = getFile(path);
        return file.list();
    }

    private void loadBackgroundsFromDirectory() {

        String pathToBGFolder = "backgrounds/";

        String[] backgrounds = findBackgrounds(pathToBGFolder);

        addAvailableBackgroundsToMenu(pathToBGFolder, backgrounds);

    }

    private void addAvailableBackgroundsToMenu(String pathToBGFolder, String[] backgrounds) {
        for (var image : backgrounds) {

            MenuItem item = new MenuItem(image);
            item.setOnAction(event -> setHBoxBackground(pathToBGFolder, image));
            backgroundMenuButton.getItems().add(item);
        }
    }

    private void setHBoxBackground(String pathToBGFolder, String image) {
        try {
            if (applicationSettings != null)
                applicationSettings.setBackground(image);

            Background background = createBackgroundFromImage(new Image(new FileInputStream(pathToBGFolder + image)));
            backgroundHBox.setBackground(background);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Background createBackgroundFromImage(Image image) {
        BackgroundSize size = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);

        return new Background(new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                size));
    }

    private void loadAndRefreshCategories() {
        try {
            loadDeserializedCategories();
        } catch (Exception e) {
            loadSampleCategories();
            throw new RuntimeException("Categories could not be loaded! " + e);
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

    private void updateTaskDescription() {
        String content = taskDescriptionTextArea.getText();
        Task task = getSelectedTask();

        if (content == null || content.isBlank() || task == null) return;

        taskDescriptionTextArea.setText(task.getDescription());

        getSelectedTask().setDescription(content);
        refreshTaskDetails();
    }

    // todo, wont just refresh work?
    private void refreshCategories() {
        categoriesListView.setItems(FXCollections.observableArrayList(categories));
    }

    private void refreshTaskList() {
        // todo: think about simplifying this
        int selectedCategoryIndex = getSelectedCategoryIndex();
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.size()) return;

        TaskList tasks = categories.get(selectedCategoryIndex);

        activeTasksListView.setItems(FXCollections.observableArrayList(tasks.getTasks()));
        updateContentOfCategoryLabel();

        completedTasksListView.refresh();

    }

    private void refreshTaskDetails() {
        if (getSelectedTask() == null) return;
        updateTaskDescriptionInDetailsPane();
        updateContentOfTaskTitleLabel();
        updateContentOfDatePicker();
    }

    private void updateContentOfDatePicker() {
        addDueDatePicker.setValue(getSelectedTask().getDate());
    }

    private void updateContentOfCategoryLabel() {
        taskListLabel.setText(getSelectedCategoryItem().getTaskListTitle());
    }

    private void updateContentOfTaskTitleLabel() {
        Task task = getSelectedTask();

        String title = task.getTitle() == null ? "" : task.getTitle();

        taskTitleLabel.setText(title);
    }

    private void updateTaskDescriptionInDetailsPane() {

        String description = getSelectedTask().getDescription();

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

    private void clearAddTaskTextFiled() {
        addTaskTextField.setText("");
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
        serializeObject(categories, path);
    }

    private void serializeCompletedTasks() {
        String path = "programData/completed";
        ArrayList<Task> completedTasks = new ArrayList<>(completedTasksListView.getItems());
        serializeObject(completedTasks, path);
    }

    private void serializeApplicationSettings() {
        String path = "programData/settings";
        serializeObject(this.applicationSettings, path);
    }

    private ApplicationSettings deserializeApplicationSettings() {
        String path = "programData/settings";
        return (ApplicationSettings) deserializeObject(path);
    }


    @SuppressWarnings("unchecked")
    private ArrayList<TaskList> deserializeCategories() {
        return (ArrayList<TaskList>) deserializeObject("programData/categories");
    }


    @SuppressWarnings("unchecked")
    private ArrayList<Task> deserializeCompletedTasks() {
       return  (ArrayList<Task>) deserializeObject("programData/completed");
    }


    private Object deserializeObject(String objectPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(objectPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            Object deserializedObject = objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();

            return deserializedObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void serializeObject(Object toSerialize, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(toSerialize);

            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
