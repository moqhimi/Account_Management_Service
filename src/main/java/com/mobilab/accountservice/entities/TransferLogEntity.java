package com.mobilab.accountservice.entities;

public class TransferLogEntity extends FinancialLogEntity{
	
	private static final long serialVersionUID = -6597784790789718123L;
	
	protected String destinationAccountNo;
	
	public TransferLogEntity() {
		
	}

	public TransferLogEntity(String accountNo, Long timestamp, Long transactionID, 
			String sender, short type, boolean status, Double balanceBefore, 
			Double balanceAfter, String destinationAccountNo) {
		super(accountNo, timestamp, transactionID, sender, type, status, balanceBefore, balanceAfter);
		this.destinationAccountNo = destinationAccountNo;
	}

	public String getDestinationAccountNo() {
		return destinationAccountNo;
	}

	public void setDestinationAccountNo(String destinationAccountNo) {
		this.destinationAccountNo = destinationAccountNo;
	}
	
	@Override
	public String toString() {
		return "TransferLogEntity [destinationAccountNo=" + destinationAccountNo + ", destinationBalanceBefore="
				+ ", balanceBefore="
				+ balanceBefore + ", balanceAfter=" + balanceAfter + ", timestamp=" + timestamp + 
				", transactionID=" + transactionID + ", sender=" + sender + ", type=" + type
				+ ", accountNo=" + accountNo + ", status=" + status + "]";
	}


	

}
