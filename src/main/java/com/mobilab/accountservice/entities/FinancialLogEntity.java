package com.mobilab.accountservice.entities;

public class FinancialLogEntity extends LogEntity{

	private static final long serialVersionUID = 3098816611278523960L;

	protected Double balanceBefore;
	
	protected Double balanceAfter;

	public FinancialLogEntity() {
		
	}

	public FinancialLogEntity(String accountNo, Long timestamp, 
			Long transactionID, String sender, short type, boolean status,
			Double balanceBefore, Double balanceAfter) {
		super(accountNo, timestamp, transactionID, sender, type, status);
		this.balanceBefore = balanceBefore;
		this.balanceAfter = balanceAfter;
	}

	public Double getBalanceBefore() {
		return balanceBefore;
	}

	public void setBalanceBefore(Double balanceBefore) {
		this.balanceBefore = balanceBefore;
	}

	public Double getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(Double balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	@Override
	public String toString() {
		return "FinancialLogEntity [balanceBefore=" + balanceBefore + ", balanceAfter=" + balanceAfter + ", timestamp="
				+ timestamp + ", transactionID=" + transactionID + ", sender=" + sender
				+ ", type=" + type + ", accountNo=" + accountNo + ", status=" + status + "]";
	}

	

}
