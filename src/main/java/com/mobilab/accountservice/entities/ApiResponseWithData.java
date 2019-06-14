package com.mobilab.accountservice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * Holds api response with data
 */
@JsonRootName("api_response")
public class ApiResponseWithData<T> extends ApiResponse {
	@JsonProperty("data")
	private T data;
	@JsonProperty("transaction_id")
	private long transactionId;
	 @JsonProperty("transaction_result_url")
	private String transactionResultUrl;

	private ApiResponseWithData() {
		super();
	}

	protected ApiResponseWithData(String errorMessage, boolean isError) {
		super(errorMessage);
		this.setError(isError);
	}

	public ApiResponseWithData(T data) {
		this();
		this.data = data;
	}

	public ApiResponseWithData(long transactionId, String transactionResultUrl) {
		this();
		this.transactionId = transactionId;
		this.transactionResultUrl = transactionResultUrl;
	}

	public ApiResponseWithData(T data, long transactionId) {
		this();
		this.data = data;
		this.transactionId = transactionId;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override public String toString() {
		return "ApiResponseWithData{" + "data=" + data + ", transactionId=" + transactionId + ", transactionResultUrl='" + transactionResultUrl + '\'' + '}';
	}
}
