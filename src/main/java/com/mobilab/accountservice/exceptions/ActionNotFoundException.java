package com.mobilab.accountservice.exceptions;

public class ActionNotFoundException extends RuntimeException {
	public ActionNotFoundException() {
		super();
	}

	public ActionNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionNotFoundException(final String message) {
		super(message);
	}

	public ActionNotFoundException(final Throwable cause) {
		super(cause);
	}
}

