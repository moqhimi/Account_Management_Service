package com.mobilab.accountservice.bussiness.message;

import com.mobilab.accountservice.entities.Account;

/**
 * a Factory class to create any type of response in business handler
 */

public class ResponseFactory {
	
	/**
	 * creates a withdrawal message response.
	 * @param accountNo - account number for withdrawal
	 * @param newBalance - balance of the account after withdrawal
	 */
	public static MyResponse withdrawal(long id, Long gateID, String sender, short status,
			String error, String accountNo, Double newBalance) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_WITHDRAWAL, id, gateID, sender, 
				System.currentTimeMillis(), status, error, 
				AccountFactory.account(accountNo, newBalance));
	}
	
	/**
	 * creates a deposit message response.
	 * @param accountNo - account number for deposit
	 * @param newBalance - balance of the account after deposit
	 */
	public static MyResponse deposit(long id, Long gateID, String sender, short status,
			String error, String accountNo, Double newBalance) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_DEPOSIT, id, gateID, sender, 
				System.currentTimeMillis(), status, error, 
				AccountFactory.account(accountNo, newBalance));
	}
	
	/**
	 * creates a transfer message response.
	 * @param accountNo - account number for transfer
	 * @param newBalance - balance of the account after transfer
	 */
	public static MyResponse transfer(long id, Long gateID, String sender, short status,
			String error, String accountNo, Double newBalance) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_TRANSFER, id, gateID, sender, 
				System.currentTimeMillis(), status, error, 
				AccountFactory.account(accountNo, newBalance));
	}
	
	/**
	 * creates a read account message response.
	 * @param accountNo - account number to read
	 * @param account - returned account object
	 */
	public static MyResponse read(long id, Long gateID, String sender, short status,
			String error, Account account) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_READ_ACCOUNT, id, gateID, sender, 
				System.currentTimeMillis(), status, error, account);
	}
	
	/**
	 * creates a create account message response.
	 * @param accountNo - account number to create
	 * @param account - created account. in case of failure, it only contains the original request info.
	 */
	public static MyResponse create(long id, Long gateID, String sender, short status,
			String error, Account account) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_CREATE_ACCOUNT, id, gateID, sender, 
				System.currentTimeMillis(), status, error, account);
	}
	
	/**
	 * creates a update account message response.
	 * @param accountNo - account number to update
	 * @param account - updated version of account
	 */
	public static MyResponse update(long id, Long gateID, String sender, short status,
			String error, Account account) {
		return new MyResponse(MyResponse.RESPONSE_TYPE_UPDATE_ACCOUNT, id, gateID, sender, 
				System.currentTimeMillis(), status, error, account);
	}
	
	
}
