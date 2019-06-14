package com.mobilab.accountservice.bussiness.message;

import java.io.Serializable;

import com.mobilab.accountservice.entities.Account;

/**
 * encapsulates the response which is sent from business handler to gateway.
 * it contains all of the fields required in every kind of response, 
 * without the need to serialize null objects.
 *
 */

public class MyResponse implements Serializable, PartitioningKeyGetter{

	private static final long serialVersionUID = -1685169833836386596L;
	
	/**
	 * each response type is corresponding to the same request type
	 */
	public static final short RESPONSE_TYPE_WITHDRAWAL = 1;
	public static final short RESPONSE_TYPE_DEPOSIT = 2;
	public static final short RESPONSE_TYPE_TRANSFER = 3;
	public static final short RESPONSE_TYPE_READ_ACCOUNT = 4;
	public static final short RESPONSE_TYPE_CREATE_ACCOUNT = 5;
	public static final short RESPONSE_TYPE_UPDATE_ACCOUNT = 6;
	
	/**
	 * status of response, which shows the cause of the failure if any 
	 * problem or validation error takes place.
	 */
	public static final short STATUS_OK = 1;
	/**
	 * might be triggered in read or update or financial requests
	 */
	public static final short STATUS_ACCOUNT_DOESNT_EXIST = 2;
	/**
	 * might be triggered in withdrawal and transfer requests
	 */
	public static final short STATUS_AMOUNT_EXCEEDS = 3;
	/**
	 * might be triggered in any kind of requests especially in creating a duplicate account
	 */
	public static final short STATUS_DATABASE_ERROR = 4;
	public static final short STATUS_TIMEOUT = 5;
	/**
	 * denotes any other exception
	 */
	public static final short STATUS_FAILURE = 6;
	
	private short responseType;
	
	/**
	 * message id , assigned by gate.
	 * each response id is same as its request id.
	 */
	private long id;
	
	/**
	 * id of the gate process.
	 */
	private Long gateID;
	
	/**
	 * end user id, IP or name
	 */
	private String sender;
	
	/**
	 * time when request arrived
	 */
	private long timestamp;
	
	/**
	 * status of response, for example STATUS_DATABASE_ERROR
	 */
	private short status;
	
	/**
	 * if an exception occur, is its message
	 */
	private String error;
	
	/**
	 * for read request is search result.
	 * for update request is updated account
	 * for create request is created account
	 * for withdrawal / deposit / transfer requests contains only new balance
	 */
	private Account account;
	
	public MyResponse() {
		
	}
	
	public MyResponse(short responseType, long id, Long gateID, String sender, long timestamp, short status,
			String error, Account account) {
		this.responseType = responseType;
		this.id = id;
		this.gateID = gateID;
		this.sender = sender;
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.account = account;
	}

	public short getResponseType() {
		return responseType;
	}

	public void setResponseType(short responseType) {
		this.responseType = responseType;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getGateID() {
		return gateID;
	}

	public void setGateID(Long gateID) {
		this.gateID = gateID;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public String toString() {
		return "MyResponse [responseType=" + responseType + ", id=" + id + ", gateID=" + gateID + ", sender=" + sender
				+ ", timestamp=" + timestamp + ", status=" + status + ", error=" + error
				+ ", account=" + account + "]";
	}
	
	/**
	 * to which gate partitions it should be redirected
	 * @return - Long (the key which is used in Kafka producers)
	 */
	@Override
	public Long partitioningKey() {
		return Long.valueOf(new AccountKey(this.account.getAccountInfo().getAccountNo()).hashCode());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyResponse other = (MyResponse) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

}
