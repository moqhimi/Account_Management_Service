package com.mobilab.accountservice.entities;

import javax.validation.constraints.NotNull;
/**
 * Holds Account Information for a bank account
 */
public class AccountInfo {
	// could be 1 and 2 , not used in bossiness side now
	private Short type;
	private String accountNo;
	@NotNull(message="currency cannot be missing or empty")
	private String currency;
	private String createdAt;


	public static final short TYPE_SAVINGS = 1;
	public static final short TYPE_LOAD = 2;

	public AccountInfo(Short type, String accountNo, String currency, String createdAt) {
		this.type = type;
		this.accountNo = accountNo;
		this.currency = currency;
		this.createdAt = createdAt;
	}

	public AccountInfo() {
		type=1;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNo == null) ? 0 : accountNo.hashCode());
		return Math.abs(result);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountInfo other = (AccountInfo) obj;
		if (accountNo == null) {
			if (other.accountNo != null)
				return false;
		} else if (!accountNo.equals(other.accountNo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AccountInfo [type=" + type + ", accountNo=" + accountNo + ", currency=" + currency + ", createdAt="
				+ createdAt + "]";
	}
	
	
}
