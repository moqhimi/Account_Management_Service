package com.mobilab.accountservice.entities;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Account {

	private Long accountID;
	@NotNull(message="balance cannot be missing or empty")
	private Double balance;
	@NotNull(message="personalInfo cannot be missing or empty")
	@Valid
	private PersonalInfo personalInfo;
	@NotNull(message="accountInfo cannot be missing or empty")
	@Valid
	private AccountInfo accountInfo;
	public Account() {
	}
	public Account(Long accountID, Double balance, PersonalInfo personalInfo, AccountInfo accountInfo) {
		this.accountID = accountID;
		this.balance = balance;
		this.personalInfo = personalInfo;
		this.accountInfo = accountInfo;
	}

	public Long getAccountID() {
		return accountID;
	}

	public void setAccountID(Long accountID) {
		this.accountID = accountID;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public PersonalInfo getPersonalInfo() {
		return personalInfo;
	}

	public void setPersonalInfo(PersonalInfo personalInfo) {
		this.personalInfo = personalInfo;
	}

	public AccountInfo getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}

	@Override
	public String toString() {
		return "Account [accountID=" + accountID + ", balance=" + balance + ", personalInfo=" + personalInfo + ", accountInfo="
				+ accountInfo + "]";
	}

	@Override
	public int hashCode() {
		return ((accountInfo == null) ? 31 : accountInfo.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (accountInfo == null) {
			if (other.accountInfo != null)
				return false;
		} else if (!accountInfo.equals(other.accountInfo))
			return false;
		return true;
	}

	

}