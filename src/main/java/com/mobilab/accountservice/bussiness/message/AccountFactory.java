package com.mobilab.accountservice.bussiness.message;

import java.util.Random;

import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.PersonalInfo;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Factory class for unique account construction
 */

public class AccountFactory {
	
	private static final Random RANDOM = new Random();
	
	/**
	 * generate a random accountID when user wants to create a new account
	 */
	public static Long accountID() {
		return RANDOM.nextLong();
	}

	/**
	 * get the key for an account to enable partitioning by its hashCode and also usage of its hashCode
	 * enable us to manage lists , maps
	 *
	 * @param account - account to get the key
	 * @return - AccountKey (represents unique key with hashCode and equals method)
	 */
	public static AccountKey accountKey(Account account) {
		return new AccountKey(account.getAccountInfo().getAccountNo());
	}
	
	/**
	 * create an account object by accountNo and its balance, usually used in financial business logic
	 * to have information about balance and also have accountNo to perform queries and so on.
	 *
	 * @param accountNo - unique account number
	 * @param balance - account balance
	 * @return - Account
	 */
	public static Account account(String accountNo, Double balance) {
		Account a = new Account();
		a.setAccountInfo(new AccountInfo(null, accountNo, null, null));
		a.setBalance(balance);
		return a;
	}
	
	/**
	 * create an account object by only accountNo field. for example when it comes to search an account,
	 * the only field to pass is an accountNo, so one Account object will be created by this field
	 *
	 * @param accountNo - unique account number
	 * @return - Account
	 */
	public static Account account(String accountNo) {
		return new Account(RANDOM.nextLong(), null, new PersonalInfo(), new AccountInfo(null, accountNo, null, null));
	}
	
	/**
	 * create an account object by all the fields
	 */
	public static Account account(Long accountID, Double balance, String firstname, String lastname,String birthdate,
			String passportNo, String address, String email, String phoneNumber, String mobileNumber,
			Short type, String accountNo, String currency, String createdAt) {
		return new Account(accountID, balance, 
				new PersonalInfo(firstname, lastname, birthdate, passportNo, address,
						email, phoneNumber, mobileNumber), 
				new AccountInfo(type, accountNo, currency, createdAt));
	}
	
	/**
	 * create an account object by given personal info fields
	 * @return - ApiResponse (transaction result references)
	 */
	public static Account account(String firstname, String lastname,String birthdate,
			String passportNo, String address, String email, String phoneNumber, String mobileNumber) {
		return new Account(RANDOM.nextLong(), null, 
				new PersonalInfo(firstname, lastname, birthdate, passportNo, address, 
						email, phoneNumber, mobileNumber), null);
	}
	
	/**
	 * create an account object by given fields
	 */
	public static Account account(Double balance, Short type, String accountNo, String currency, 
			String createdAt) {
		return new Account(RANDOM.nextLong(), balance, 
				new PersonalInfo(), 
				new AccountInfo(type, accountNo, currency, createdAt));
	}
	
	/**
	 * create an account object by given fields
	 */
	public static Account account(Double balance, Short type, String accountNo, String currency) {
		return new Account(RANDOM.nextLong(), balance, 
				new PersonalInfo(), 
				new AccountInfo(type, accountNo, currency, null));
	}
	
	/**
	 * create an account object by given fields
	 */
	public static Account account(Double balance, String firstname, String lastname, 
			String phoneNumber, 
			Short type, String accountNo, String currency) {
		return new Account(RANDOM.nextLong(), balance, 
				new PersonalInfo(firstname, lastname, null, null, null, 
						null, phoneNumber, null), 
				new AccountInfo(type, accountNo, currency, null));
	}
	
	/**
	 * get the currency of an account and return USD as default if null
	 *
	 * @param a - account to be get its currency
	 */
	public static String currency(Account a) {
		if(a==null || a.getAccountInfo()==null || a.getAccountInfo().getCurrency()==null) {
			return "USD";
		}
		return a.getAccountInfo().getCurrency();
	}

	/**
	 * generate a random account number when we want to create a new account
	 */
	public static String generateAccountNumber(){
		String startWith = RandomStringUtils.randomAlphabetic(2).toUpperCase();
		return String.format("%s%d",startWith,RANDOM.nextInt());
	}
	
}
