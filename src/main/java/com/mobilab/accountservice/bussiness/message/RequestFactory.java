package com.mobilab.accountservice.bussiness.message;

import com.mobilab.accountservice.entities.Account;

/**
 * a Factory class to create any type of request in gate
 */

public class RequestFactory {
	
	/**
	 * creates a withdrawal message request.
	 * @param accountNo - account number for withdrawal
	 * @param amount - amount of money for withdrawal
	 */
	public static MyRequest withdrawal(long id, Long gateID, String sender, 
			String accountNo,  double amount) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(AccountFactory.account(accountNo));
		r.setFinancialAmount(amount);
		r.setRequestType(MyRequest.REQUEST_TYPE_WITHDRAWAL);
		r.setReturnUpdated(true);
		r.setTypeUpdate(MyRequest.TYPE_ONLY_BALANCE);
		r.setTypeReturned(MyRequest.TYPE_ONLY_BALANCE);
		return r;
	}
	
	/**
	 * creates a deposit message request.
	 * @param accountNo - account number for deposit
	 * @param amount - amount of money for deposit
	 */
	public static MyRequest deposit(long id, Long gateID, String sender, 
			String accountNo, double amount) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(AccountFactory.account(accountNo));
		r.setFinancialAmount(amount);
		r.setRequestType(MyRequest.REQUEST_TYPE_DEPOSIT);
		r.setReturnUpdated(true);
		r.setTypeUpdate(MyRequest.TYPE_ONLY_BALANCE);
		r.setTypeReturned(MyRequest.TYPE_ONLY_BALANCE);
		return r;
	}
	
	/**
	 * creates a transfer message request.
	 * @param accountNo - source account number
	 * @param amount - amount of money to transfer
	 * @param accountNo2 - destination account number
	 * @param balance - source account balance (is null when gate creates it)
	 */
	public static MyRequest transfer(long id, Long gateID, String sender, 
			String accountNo, double amount,
			String accountNo2, Double balance) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(AccountFactory.account(accountNo, balance));
		r.setFinancialAmount(amount);
		r.setFinancialAccountNo2(accountNo2);
		r.setRequestType(MyRequest.REQUEST_TYPE_TRANSFER);
		r.setReturnUpdated(true);
		r.setTypeUpdate(MyRequest.TYPE_ONLY_BALANCE);
		r.setTypeReturned(MyRequest.TYPE_ONLY_BALANCE);
		return r;
	}
	
	/**
	 * creates a read account message request.
	 */
	public static MyRequest read(long id, Long gateID, String sender, 
			String accountNo) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(AccountFactory.account(accountNo));
		r.setRequestType(MyRequest.REQUEST_TYPE_READ_ACCOUNT);
		r.setTypeUpdate(MyRequest.TYPE_NONE);
		r.setTypeReturned(MyRequest.TYPE_ALL);
		return r;
	}
	
	/**
	 * creates a create account message request.
	 */
	public static MyRequest create(long id, Long gateID, String sender, 
			Account account) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(account);
		r.setRequestType(MyRequest.REQUEST_TYPE_CREATE_ACCOUNT);
		r.setReturnUpdated(true);
		r.setTypeUpdate(MyRequest.TYPE_NONE);
		r.setTypeReturned(MyRequest.TYPE_ALL);
		return r;
	}
	
	/**
	 * creates an update account message request.
	 */
	public static MyRequest update(long id, Long gateID, String sender, 
			Account account) {
		final MyRequest r = new MyRequest();
		r.setId(id);
		r.setGateID(gateID);
		r.setSender(sender);
		r.setTimestamp(System.currentTimeMillis());
		r.setAccount(account);
		r.setRequestType(MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT);
		r.setReturnUpdated(true);
		r.setTypeUpdate(MyRequest.TYPE_ALL);
		r.setTypeReturned(MyRequest.TYPE_ALL);
		return r;
	}


	
}
