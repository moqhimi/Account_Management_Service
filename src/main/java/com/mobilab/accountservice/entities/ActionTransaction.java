package com.mobilab.accountservice.entities;

/**
 * Holds transaction request for all financial transaction
 */
public class ActionTransaction {
	// deposit, withdrawal and transfer (case insensitive)
	private String action;
	private String destinationAccount;
	private Double value;
	
	public ActionTransaction() {
	}
	
	public ActionTransaction(String action, String destinationAccount, Double value) {
		this.action = action;
		this.destinationAccount = destinationAccount;
		this.value = value;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDestinationAccount() {
		return destinationAccount;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
