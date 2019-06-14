package com.mobilab.accountservice.entities;
/**
 * Holds log request information
 */
public class LogRequest {
	private String startDate;
	private String endDate;
	private Long transactionId;
	private Boolean desc;

	public LogRequest() {
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Boolean isDesc() {
		return desc;
	}

	public void setDesc(Boolean desc) {
		this.desc = desc;
	}



	@Override public String toString() {
		return "LogRequest{" + "startDate='" + startDate + '\'' + ", endDate='" + endDate + '\'' + ", transactionId=" + transactionId + '}';
	}
}
