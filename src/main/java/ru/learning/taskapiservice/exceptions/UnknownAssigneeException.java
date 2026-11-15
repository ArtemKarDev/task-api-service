package ru.learning.taskapiservice.exceptions;

public class UnknownAssigneeException extends RuntimeException {

    public UnknownAssigneeException(String assignee) {
        super("Исполнитель не найден в справочнике сотрудников: " + assignee);
    }
}