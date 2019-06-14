package com.mobilab.accountservice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Holds api response parent object
 */
@JsonRootName("api_response")
public class ApiResponse {
	@JsonProperty("error_message")
	private String errorMessage;
	@JsonProperty("error")
	private Boolean error;

	public static final boolean ERROR = true;
	public static final boolean SUCCESS = false;
	private static final String EMPTY_STRING = "";

	public ApiResponse() {
		errorMessage = EMPTY_STRING;
		error = SUCCESS;
	}

	public ApiResponse(String errorMessage) {
		this.error = ERROR;
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setErrorMessageAndSetErrorToTrue(String errorMessage) {
		this.error = ERROR;
		this.errorMessage = errorMessage;
	}

	public Boolean isError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	@Override public String toString() {
		return "ApiResponse{" + "errorMessage='" + errorMessage + '\'' + ", error=" + error + '}';
	}
}