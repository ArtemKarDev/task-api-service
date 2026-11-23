package ru.learning.taskapiservice.exceptions;

/** Действие запрещено для учебного (публичного) пользователя. */
public class DemoAccountException extends RuntimeException {

    public DemoAccountException() {
        super("This action is not allowed for the demo account");
    }
}