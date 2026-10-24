package ru.learning.taskapiservice.entity;

import lombok.Data;

@Data
public class TaskForm {

    private String title;
    private String description;
    private String assignee;
    private Priority priority;
    private TaskStatus status;

}
