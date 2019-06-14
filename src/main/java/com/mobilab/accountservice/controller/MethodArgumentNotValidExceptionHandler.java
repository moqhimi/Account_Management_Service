package com.mobilab.accountservice.controller;

import com.mobilab.accountservice.entities.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class MethodArgumentNotValidExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<org.springframework.validation.FieldError> fieldErrors = result.getFieldErrors();
        return processFieldErrors(fieldErrors);
    }

    private ApiResponse processFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Validation Error: ");
        int i = 0;
        for (org.springframework.validation.FieldError fieldError : fieldErrors) {
            errorMessage.append(fieldError.getDefaultMessage());
            if (i != fieldErrors.size() - 1) {
                errorMessage.append(", ");
            }
        }
        return new ApiResponse(errorMessage.toString());
    }
}