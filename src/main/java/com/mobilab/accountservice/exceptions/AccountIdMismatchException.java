package com.mobilab.accountservice.exceptions;

public class AccountIdMismatchException extends RuntimeException {

    public AccountIdMismatchException() {
        super();
    }

    public AccountIdMismatchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccountIdMismatchException(final String message) {
        super(message);
    }

    public AccountIdMismatchException(final Throwable cause) {
        super(cause);
    }
}