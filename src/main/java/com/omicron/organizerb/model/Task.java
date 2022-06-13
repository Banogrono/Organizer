/**
 * Part of test
 * Created by: @Author V
 * Date: @Date 04-Jun-22
 * Time: 21:10
 * =============================================================
 **/

package com.omicron.organizerb.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class Task {

    // ========================================================================================
    // Fields
    // ========================================================================================

    private String title;

    private String description;

    private List<Task> subTasks;

    private RepeatTask repetition = RepeatTask.NONE;

    private LocalDate date = LocalDate.now();

    private LocalTime time = LocalTime.now();

    private TaskPriority priority = TaskPriority.NORMAL;

    private List<String> tags;

    private boolean isDone = false;

    // ========================================================================================
    // Constructors
    // ========================================================================================


    public Task(String title, String description, List<Task> subTasks, RepeatTask repetition, LocalDate date, LocalTime time, TaskPriority priority, List<String> tags) {
        this.title = title;
        this.description = description;
        this.subTasks = subTasks;
        this.repetition = repetition;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.tags = tags;
    }

    public Task(String title) {
        this.title = title;
    }

    public Task() {
    }

    // ========================================================================================
    // Getters & Setters
    // ========================================================================================

    // generated by Lombok

    // ========================================================================================
    // Methods
    // ========================================================================================

    @Override
    public String toString() {
        return title;
    }

    public String toStringFullDetails() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subTasks=" + subTasks +
                ", repeat=" + repetition +
                ", date=" + date +
                ", time=" + time +
                ", priority=" + priority +
                ", tags=" + tags +
                '}';
    }


}
