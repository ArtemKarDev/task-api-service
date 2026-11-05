package ru.learning.taskapiservice.exceptions;

/** Ошибка смены пароля с причиной: страница по ней показывает сообщение под нужным полем. */
public class PasswordChangeException extends InvalidRequestException {

    public enum Reason {
        WRONG_CURRENT,
        SAME_AS_CURRENT
    }

    private final Reason reason;

    public PasswordChangeException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}