package com.mobilab.accountservice.entities;

public class UpdateLogEntity extends LogEntity{
	
	private static final long serialVersionUID = -2337982847264519237L;
	
	private Account accountBefore;
	
	private Account accountAfter;
	
	public UpdateLogEntity() {
		
	}

	public UpdateLogEntity(String accountNo, Long timestamp, Long transactionID, String sender,
			short type, boolean status, Account accountBefore, Account accountAfter) {
		super(accountNo, timestamp, transactionID, sender, type, status);
		this.accountBefore = accountBefore;
		this.accountAfter = accountAfter;
	}

	public Account getAccountBefore() {
		return accountBefore;
	}

	public void setAccountBefore(Account accountBefore) {
		this.accountBefore = accountBefore;
	}

	public Account getAccountAfter() {
		return accountAfter;
	}

	public void setAccountAfter(Account accountAfter) {
		this.accountAfter = accountAfter;
	}

	@Override
	public String toString() {
		return "UpdateLogEntity [accountBefore=" + accountBefore + ", accountAfter=" + accountAfter + ", timestamp="
				+ timestamp + ", transactionID=" + transactionID + ", sender=" + sender
				+ ", type=" + type + ", accountNo=" + accountNo + ", status=" + status + "]";
	}



}
