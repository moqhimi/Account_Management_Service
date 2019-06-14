package com.mobilab.accountservice.controller;


import com.mobilab.accountservice.exceptions.AccountIdMismatchException;
import com.mobilab.accountservice.exceptions.AccountNotFoundException;
import com.mobilab.accountservice.exceptions.ActionNotFoundException;
import com.mobilab.accountservice.exceptions.WrongValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 *
 * This class is used to override spring exception handling for our custom exceptions such as
 *  Account noot found excetion, Action not found exception, ....
 *  need to be revised later
 */

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	public RestExceptionHandler() {
		super();
	}

	@ExceptionHandler(AccountNotFoundException.class)
	protected ResponseEntity<Object> handleAccountNotFound(Exception ex, WebRequest request) {
		return handleExceptionInternal(ex, "Account not found", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	@ExceptionHandler(ActionNotFoundException.class)
	protected ResponseEntity<Object> handleActionNotFound(Exception ex, WebRequest request) {
		return handleExceptionInternal(ex, "Action not found", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler({
			AccountIdMismatchException.class,
			ConstraintViolationException.class,
			DataIntegrityViolationException.class,
			WrongValueException.class
	})
	public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
		return handleExceptionInternal(ex, ex
				.getLocalizedMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
}