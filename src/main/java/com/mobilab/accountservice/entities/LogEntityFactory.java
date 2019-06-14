package com.mobilab.accountservice.entities;

import com.mobilab.accountservice.bussiness.message.MyRequest;

public class LogEntityFactory {

	public static FinancialLogEntity withdrawal(MyRequest request, boolean status, 
			Double balanceBefore, Double balanceAfter) {
		return new FinancialLogEntity(request.getAccount().getAccountInfo().getAccountNo(), 
				request.getTimestamp(), request.getId(), request.getSender(), 
				LogEntity.TYPE_WITHDRAWAL, status, balanceBefore, balanceAfter);
	}
	
	public static FinancialLogEntity deposit(MyRequest request, boolean status, 
			Double balanceBefore, Double balanceAfter) {
		return new FinancialLogEntity(request.getAccount().getAccountInfo().getAccountNo(), 
				request.getTimestamp(), request.getId(), request.getSender(), 
				LogEntity.TYPE_DEPOSIT, status, balanceBefore, balanceAfter);
	}
	
	public static TransferLogEntity transfer(MyRequest request, boolean status, 
			Double balanceBefore, Double balanceAfter, String destinationAccountNo) {
		return new TransferLogEntity(request.getAccount().getAccountInfo().getAccountNo(), 
				request.getTimestamp(), request.getId(), request.getSender(), 
				LogEntity.TYPE_TRANSFER, status, balanceBefore, balanceAfter, 
				destinationAccountNo);
	}
	
	public static UpdateLogEntity create(MyRequest request, boolean status, Account insertedAccount) {
		return new UpdateLogEntity(request.getAccount().getAccountInfo().getAccountNo(), 
				request.getTimestamp(), request.getId(), request.getSender(), 
				LogEntity.TYPE_CREATE_ACCOUNT, status, null, insertedAccount);
	}
	
	public static UpdateLogEntity update(MyRequest request, boolean status, Account accountBefore,
			Account accountAfter) {
		return new UpdateLogEntity(request.getAccount().getAccountInfo().getAccountNo(), 
				request.getTimestamp(), request.getId(), request.getSender(), 
				LogEntity.TYPE_UPDATE_ACCOUNT, status, accountBefore, accountAfter);
	}
	
}
