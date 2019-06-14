package com.mobilab.accountservice.bussiness.message;

import com.mobilab.accountservice.entities.Account;

import java.io.Serializable;

/**
 * encapsulates any request which is sent from gateway to business handler.
 * it contains all information required in any type of the request.
 * the nullable fields could be null and there is no need to serialize them in Kafka topics.
 */

public class MyRequest implements Serializable, PartitioningKeyGetter{
	
	private static final long serialVersionUID = 7269757532294474877L;
	
	/**
	 * specifies which parts of account structure should be updated / read / returned.
	 * for example, if type=1 then only update / read / return balance field.
	 */
	public static final short TYPE_NONE = 0;
	public static final short TYPE_ONLY_BALANCE = 1;
	public static final short TYPE_ONLY_PERSONAL_INFO = 3;
	public static final short TYPE_ONLY_ACCOUNT_INFO = 3;
	public static final short TYPE_ALL = 4;
	
	/**
	 * the type of request
	 */
	public static final short REQUEST_TYPE_WITHDRAWAL = 1;
	public static final short REQUEST_TYPE_DEPOSIT = 2;
	public static final short REQUEST_TYPE_TRANSFER = 3;
	public static final short REQUEST_TYPE_READ_ACCOUNT = 4;
	public static final short REQUEST_TYPE_CREATE_ACCOUNT = 5;
	public static final short REQUEST_TYPE_UPDATE_ACCOUNT = 6;
	
	/**
	 * should be one of the fields above to show the type of the request
	 */
	private short requestType;
	
	/**
	 * message id which is assigned by gate
	 */
	private long id;
	
	/**
	 * id of the gate process
	 */
	private Long gateID;
	
	/**
	 * end user id, IP or name
	 */
	private String sender;
	
	/**
	 * time stamp of message when it arrives , assigned by gate
	 */
	private long timestamp;
	
	/**
	 * if true then response includes updated account.
	 * in read requests is null.
	 */
	private Boolean returnUpdated;
	
	/**
	 * which parts should be updated in update requests, for example: TYPE_ONLY_BALANCE
	 */
	private Short typeUpdate;
	
	/**
	 * which parts should be returned in update and read requests, for example: TYPE_ONLY_BALANCE
	 */
	private Short typeReturned;
	
	/**
	 * if message is read request, it only contains accountNo.
	 * if message is update request, it contains accountNo and the updated version of account object.
	 * if message is insert request, it contains accountNo and initial fields of account object.
	 * if message is withdrawal or deposit, it only contains accountNo.
	 * if message is transfer request, it only contains accountNo of the source.
	 */
	private Account account;
	
	/**
	 * in financial requests (withdrawal, deposit, transfer) shows the amount of money.
	 * in other kinds of request is null.
	 */
	private Double financialAmount;
	
	/**
	 * in transfer requests shows the accountNo of the destination account.
	 * in other kinds of request is null.
	 */
	private String financialAccountNo2;
	
	
	public MyRequest() {
		
	}

	public Double getFinancialAmount() {
		return financialAmount;
	}

	public void setFinancialAmount(Double financialAmount) {
		this.financialAmount = financialAmount;
	}

	public String getFinancialAccountNo2() {
		return financialAccountNo2;
	}

	public void setFinancialAccountNo2(String financialAccountNo2) {
		this.financialAccountNo2 = financialAccountNo2;
	}

	public short getRequestType() {
		return requestType;
	}


	public void setRequestType(short requestType) {
		this.requestType = requestType;
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

	public Boolean getReturnUpdated() {
		return returnUpdated;
	}

	public void setReturnUpdated(Boolean returnUpdated) {
		this.returnUpdated = returnUpdated;
	}

	public Short getTypeUpdate() {
		return typeUpdate;
	}

	public void setTypeUpdate(Short typeUpdate) {
		this.typeUpdate = typeUpdate;
	}

	public Short getTypeReturned() {
		return typeReturned;
	}

	public void setTypeReturned(Short typeReturned) {
		this.typeReturned = typeReturned;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Override
	public String toString() {
		return "MyRequest [requestType=" + requestType + ", id=" + id + ", gateID=" + gateID + ", sender=" + sender
				+ ", timestamp=" + timestamp + ", returnUpdated=" + returnUpdated + ", typeUpdate=" + typeUpdate
				+ ", typeReturned=" + typeReturned + ", account=" + account + ", financialAmount=" + financialAmount
				+ ", financialAccountNo2=" + financialAccountNo2
				+ "]";
	}

	/**
	 * get the unique hashCode for the account of the request.
	 * it lead Kafka partitioning to redirect requests with the same accountNo (of source) 
	 * to the same business handler
	 *
	 * @return - Long (the key which is used in Kafka producers)
	 */
	@Override
	public Long partitioningKey() {
		return Long.valueOf(AccountFactory.accountKey(this.account).hashCode());
	}
	
	/**
	 * the unique key for each request is its id
	 */
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
		MyRequest other = (MyRequest) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
