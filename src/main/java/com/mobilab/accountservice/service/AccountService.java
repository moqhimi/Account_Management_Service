package com.mobilab.accountservice.service;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.RequestFactory;
import com.mobilab.accountservice.bussiness.process.Database;
import com.mobilab.accountservice.bussiness.process.Gateway;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.ActionTransaction;
import com.mobilab.accountservice.entities.ApiResponse;
import com.mobilab.accountservice.entities.ApiResponseWithData;
import com.mobilab.accountservice.entities.MobiLabEnums;
import com.mobilab.accountservice.exceptions.AccountIdMismatchException;
import com.mobilab.accountservice.exceptions.AccountNotFoundException;
import com.mobilab.accountservice.exceptions.ActionNotFoundException;
import com.mobilab.accountservice.exceptions.WrongValueException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty("my.rest") //  has been initialized only in gateway mode
public class AccountService {
	@Autowired @Qualifier("request_gateWay")
	Gateway gateway;
	@Autowired @Qualifier("database")
	Database databaseRepo;
	private final static long range = Long.MAX_VALUE;


	private  static final String BASE_TRANSACTION_URL ="/api/v1/transactions/";

	final static Logger logger = Logger.getLogger(AccountService.class);

	/**
	 * Service method used for retrieving all accounts
	 *
	 * @return - ApiResponse (all accounts information)
	 */
	public ApiResponse readAllAccounts(){
		long transactionId = generateTID();
		List<Account> accounts = new ArrayList<>();
		databaseRepo.getAllAccounts(new Consumer<Account>() {
			@Override public void accept(Account account) {
				accounts.add(account);
			}
		});
		return new ApiResponseWithData<List<Account>>(accounts, transactionId);
	}
	/**
	 * Service method used for retrieving specific account
	 *
	 * @param id - account number
	 * @return - ApiResponse ( account information)
	 */
	public ApiResponse readAccount(String id){
		long transactionId = generateTID();
		Account account= null;
		try {
			account = databaseRepo.readAccount(AccountFactory.account(id));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(account==null){
			throw  new AccountNotFoundException();
		}
		return new ApiResponseWithData<Account>(account, transactionId);
	}
	/**
	 * Service method used for creating new account
	 *
	 * @param account - account to be created
	 * @param remoreAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	public ApiResponse createNewAccount(Account account, String remoreAddr) {
		if(account.getAccountID()==null) {
			account.setAccountID(AccountFactory.accountID());
		}
		if(account.getAccountInfo()==null){
			account.setAccountInfo(new AccountInfo());
		}
		account.getAccountInfo().setAccountNo(AccountFactory.generateAccountNumber());
		long transactionId = generateTID();
		gateway.createAccount(transactionId, remoreAddr, account);
		return new ApiResponseWithData<Account>(transactionId, BASE_TRANSACTION_URL + transactionId);
	}
	/**
	 * Service method used for updating existing account
	 *
	 * @param accountNo - account to be updated
	 * @param account - new account information
	 * @param remoteAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	public ApiResponse updateAccount(String accountNo, Account account, String remoteAddr) {
		if (!accountNo.equalsIgnoreCase(account.getAccountInfo().getAccountNo())) {
			throw new AccountIdMismatchException();
		}
		long transactionId = generateTID();
		gateway.newRequest(RequestFactory.update(transactionId, gateway.getGateID(), remoteAddr, account));
		return new ApiResponseWithData<Account>(transactionId, BASE_TRANSACTION_URL + transactionId);
	}
	/**
	 * Service method used for depositing to account
	 *
	 * @param accountNo - source account for deposit
	 * @param amount - amount
	 * @param remoteAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	private ApiResponse depositToAccount(String accountNo, double amount, String remoteAddr) {
		long transactionId = generateTID();
		gateway.newRequest(RequestFactory.deposit(transactionId, gateway.getGateID(), remoteAddr, accountNo, amount));
		return new ApiResponseWithData<Account>(transactionId, BASE_TRANSACTION_URL + transactionId);
	}

	/**
	 * Service method used for withdrawing from account
	 *
	 * @param accountNo - destination account for withdrawal
	 * @param amount - amount
	 * @param remoteAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	private ApiResponse withdrawalFromAccount(String accountNo, double amount, String remoteAddr) {
		long transactionId = generateTID();
		gateway.newRequest(RequestFactory.withdrawal(transactionId, gateway.getGateID(), remoteAddr, accountNo, amount));
		return new ApiResponseWithData<Account>(transactionId, BASE_TRANSACTION_URL + transactionId);
	}
	/**
	 * Service method used for transferring  from account to another account
	 *
	 * @param accountNo - source account for deposit
	 * @param accountNo - destination account for deposit
	 * @param amount - amount
	 * @param remoteAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	private ApiResponse transfer(String accountNo, String destAccountNo, double amount, String remoteAddr) {
		long transactionId = generateTID();
		gateway.newRequest(RequestFactory.transfer(transactionId, gateway.getGateID(), remoteAddr, accountNo, amount, destAccountNo, null));
		return new ApiResponseWithData<Account>(transactionId, BASE_TRANSACTION_URL + transactionId);
	}

	/**
	 * Service method used for handling all financial transactions
	 *
	 * @param actionTransaction - financial transaction info
	 * @param accountNo - source account Number
	 * @param remoteAddr - IP address of creator party
	 * @return - ApiResponse (transaction result references)
	 */
	public ApiResponse handleAction(ActionTransaction actionTransaction, String accountNo, String remoteAddr) {
		// request validation -- can be also moved to a new method
		if (actionTransaction == null || actionTransaction.getAction() == null) {
			throw new ActionNotFoundException();
		}
		if (Arrays.stream(MobiLabEnums.ActionType.values()).noneMatch(t -> t.getStringValue().equalsIgnoreCase(actionTransaction.getAction()))) {
			throw new ActionNotFoundException();
		}
		if (actionTransaction.getValue() == null || actionTransaction.getValue() <= 0) {
			throw new WrongValueException();
		}

		if (actionTransaction.getAction().equalsIgnoreCase(MobiLabEnums.ActionType.TRANSFER.getStringValue()) && StringUtils
				.isBlank(actionTransaction.getDestinationAccount())) {
			throw new AccountNotFoundException();
		}
		if (MobiLabEnums.ActionType.DEPOSIT.getStringValue().equalsIgnoreCase(actionTransaction.getAction())) {
			return depositToAccount(accountNo, actionTransaction.getValue(), remoteAddr);
		} else if (MobiLabEnums.ActionType.WITHDRAWAL.getStringValue().equalsIgnoreCase(actionTransaction.getAction())) {
			return withdrawalFromAccount(accountNo, actionTransaction.getValue(), remoteAddr);
		} else if (MobiLabEnums.ActionType.TRANSFER.getStringValue().equalsIgnoreCase(actionTransaction.getAction())) {
			return transfer(accountNo, actionTransaction.getDestinationAccount(), actionTransaction.getValue(), remoteAddr);
		} else {
			throw new ActionNotFoundException();
		}
	}

	/**
	 * generates a random transaction id which can be used for differentiating messages in kafka and also following up financial transactions
	 *
	 * @return - random transaction id
	 */
	private long generateTID() {
		return (long) Math.abs(RANDOM.nextLong());
	}
	
	private Random RANDOM = new Random();

	/**
	 *  Spring boot application start listener, I have started gateway threads here after all spring beans have been initialized.
	 *  Gateway threads are blocking
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		logger.info("hello world, I have just started up");
		gateway.start();

	}
	
}
