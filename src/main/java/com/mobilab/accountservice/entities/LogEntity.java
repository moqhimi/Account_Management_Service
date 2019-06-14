package com.mobilab.accountservice.entities;

import java.io.Serializable;

public class LogEntity implements Serializable{
	
	private static final long serialVersionUID = -3869937638636453794L;
	
	public static final short TYPE_WITHDRAWAL = 1;
	public static final short TYPE_DEPOSIT = 2;
	public static final short TYPE_TRANSFER = 3;
	public static final short TYPE_CREATE_ACCOUNT = 5;
	public static final short TYPE_UPDATE_ACCOUNT = 6;
	
	protected Long timestamp;
		
	protected Long transactionID;
	
	protected String sender;
	
	protected short type;
	
	protected String accountNo;
	
	//false for fail , true for success
	protected boolean status;
	
	public LogEntity() {
		
	}

	public LogEntity(String accountNo, Long timestamp, Long transactionID, 
			String sender, short type, boolean status) {
		this.timestamp = timestamp;
		this.transactionID = transactionID;
		this.sender = sender;
		this.type = type;
		this.accountNo = accountNo;
		this.status = status;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(Long transactionID) {
		this.transactionID = transactionID;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "LogEntity [timestamp=" + timestamp + ", transactionID=" + transactionID
				+ ", sender=" + sender + ", type=" + type + ", accountNo=" + accountNo + ", status=" + status + "]";
	}


}
