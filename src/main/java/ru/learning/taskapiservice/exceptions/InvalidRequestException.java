package ru.learning.taskapiservice.exceptions;

/** Запрос синтаксически верный, но по смыслу некорректен (например, PATCH без единого поля). */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}