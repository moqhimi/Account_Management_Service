package com.mobilab.accountservice.exceptions;

public class WrongValueException  extends RuntimeException{
	public WrongValueException() {
		super();
	}

	public WrongValueException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public WrongValueException(final String message) {
		super(message);
	}

	public WrongValueException(final Throwable cause) {
		super(cause);
	}
}
