package com.mobilab.accountservice;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.AccountKey;
import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.message.RequestFactory;
import com.mobilab.accountservice.bussiness.message.ResponseFactory;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.FinancialLogEntity;
import com.mobilab.accountservice.entities.LogEntity;
import com.mobilab.accountservice.entities.LogEntityFactory;
import com.mobilab.accountservice.entities.TransferLogEntity;
import com.mobilab.accountservice.entities.UpdateLogEntity;

public class FactoryTest {
	
	@Test
	public void testAccountFactory() {
		final String accountNo = AccountFactory.generateAccountNumber();
		Account account = new Account(null, null, null, new AccountInfo(null, accountNo, null, null));
		assertEquals(AccountFactory.accountKey(account).hashCode(), new AccountKey(accountNo).hashCode());
		assertEquals(AccountFactory.account(accountNo), account);
		account = AccountFactory.account(10l, 12d, "firstname", "lastname", "birthdate", 
				"passportNo", "address", "email", "phoneNumber", "mobileNumber", 
				AccountInfo.TYPE_LOAD, accountNo, "currency", "createdAt");
		assertEquals(account.getBalance().doubleValue(), 12d, 0.001d);
		assertEquals(account.getAccountID().longValue(), 10l);
		assertEquals(account.getAccountInfo().getAccountNo(), accountNo);
		assertEquals(account.getAccountInfo().getCreatedAt(), "createdAt");
		assertEquals(account.getAccountInfo().getCurrency(), "currency");
		assertEquals(account.getAccountInfo().getType().shortValue(), AccountInfo.TYPE_LOAD);
		assertEquals(account.getPersonalInfo().getAddress(), "address");
		assertEquals(account.getPersonalInfo().getEmail(), "email");
		assertEquals(account.getPersonalInfo().getMobileNumber(), "mobileNumber");
		assertEquals(account.getPersonalInfo().getPhoneNumber(), "phoneNumber");
		assertEquals(account.getPersonalInfo().getBirthdate(), "birthdate");
		assertEquals(account.getPersonalInfo().getFirstname(), "firstname");
		assertEquals(account.getPersonalInfo().getLastname(), "lastname");
		assertEquals(account.getPersonalInfo().getPassportNo(), "passportNo");
		assertEquals(account.hashCode(), AccountFactory.accountKey(account).hashCode());
		account = AccountFactory.account(1000d, Short.valueOf((short)10), accountNo, "currency", "createdAt");
		assertEquals(account.getBalance().doubleValue(), 1000d, 0.001d);
		assertEquals(account.getAccountInfo().getType().shortValue(), (short)10);
		assertEquals(account.getAccountInfo().getAccountNo(), accountNo);
		assertEquals(account.getAccountInfo().getCurrency(), "currency");
		assertEquals(account.getAccountInfo().getCreatedAt(), "createdAt");
		assertEquals(account.hashCode(), AccountFactory.accountKey(account).hashCode());
	}
	
	@Test
	public void testRequestFactory() {
		long transactionID = 1000l;
		String accountNo = AccountFactory.generateAccountNumber();
		String accountNo2 = AccountFactory.generateAccountNumber();
		long gateID = 1l;
		String sender = "sender";
		double amount = 10000d;
		double balance = 12000d;
		String currency = "USD";
		short type = AccountInfo.TYPE_LOAD;
		
		//withdrawal
		MyRequest request = RequestFactory.withdrawal(transactionID, gateID, sender, accountNo, amount);
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getFinancialAmount().doubleValue(), amount, 0.001d);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_WITHDRAWAL);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		
		//deposit
		request = RequestFactory.deposit(transactionID, gateID, sender, accountNo, amount);
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getFinancialAmount().doubleValue(), amount, 0.001d);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_DEPOSIT);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		
		//transfer
		request = RequestFactory.transfer(transactionID, gateID, sender, accountNo, amount, accountNo2, balance);
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getFinancialAmount().doubleValue(), amount, 0.001d);
		assertEquals(request.getFinancialAccountNo2(), accountNo2);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_TRANSFER);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_ONLY_BALANCE);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		
		//read
		request = RequestFactory.read(transactionID, gateID, sender, accountNo);
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_READ_ACCOUNT);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ALL);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_NONE);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		
		//update
		request = RequestFactory.update(transactionID, gateID, sender, AccountFactory.account(
				balance, type, accountNo, currency));
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ALL);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_ALL);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		assertEquals(request.getAccount().getBalance().doubleValue(), balance, 0.001d);
		assertEquals(request.getAccount().getAccountInfo().getType().shortValue(), type);
		assertEquals(request.getAccount().getAccountInfo().getCurrency(), currency);
		
		//insert
		request = RequestFactory.create(transactionID, gateID, sender, AccountFactory.account(
				balance, type, accountNo, currency));
		assertEquals(request.getId(), transactionID);
		assertEquals(request.getRequestType(), MyRequest.REQUEST_TYPE_CREATE_ACCOUNT);
		assertEquals(request.getSender(), sender);
		assertEquals(request.getTypeReturned().shortValue(), MyRequest.TYPE_ALL);
		assertEquals(request.getTypeUpdate().shortValue(), MyRequest.TYPE_NONE);
		assertEquals(request.getAccount(), AccountFactory.account(accountNo));
		assertEquals(request.getAccount().getBalance().doubleValue(), balance, 0.001d);
		assertEquals(request.getAccount().getAccountInfo().getType().shortValue(), type);
		assertEquals(request.getAccount().getAccountInfo().getCurrency(), currency);
	}
	
	@Test
	public void testResponseFactory() {
		long transactionID = 1000000l;
		String accountNo = AccountFactory.generateAccountNumber();
		long gateID = 10l;
		String sender = "sender";
		double balance = 12000d;
		String currency = "USD";
		short type = AccountInfo.TYPE_LOAD;
		short status = MyResponse.STATUS_FAILURE;
		String error = "error";
		double newBalance = 17000d;
		
		//withdrawal
		MyResponse response = ResponseFactory.withdrawal(transactionID, gateID, sender, 
				status, error, accountNo, newBalance);
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_WITHDRAWAL);
		assertEquals(response.getAccount().getBalance().doubleValue(), newBalance, 0.001d);
		
		//deposit
		response = ResponseFactory.deposit(transactionID, gateID, sender, 
				status, error, accountNo, newBalance);
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_DEPOSIT);
		assertEquals(response.getAccount().getBalance().doubleValue(), newBalance, 0.001d);
		
		//transfer
		response = ResponseFactory.transfer(transactionID, gateID, sender, 
				status, error, accountNo, newBalance);
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_TRANSFER);
		assertEquals(response.getAccount().getBalance().doubleValue(), newBalance, 0.001d);
		
		//read
		response = ResponseFactory.read(transactionID, gateID, sender, 
				status, error, AccountFactory.account(
						balance, type, accountNo, currency));
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_READ_ACCOUNT);
		assertEquals(response.getAccount().getBalance().doubleValue(), balance, 0.001d);
		assertEquals(response.getAccount().getAccountInfo().getType().shortValue(), type);
		assertEquals(response.getAccount().getAccountInfo().getCurrency(), currency);
		
		//update
		response = ResponseFactory.update(transactionID, gateID, sender, 
				status, error, AccountFactory.account(
						balance, type, accountNo, currency));
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_UPDATE_ACCOUNT);
		assertEquals(response.getAccount().getBalance().doubleValue(), balance, 0.001d);
		assertEquals(response.getAccount().getAccountInfo().getType().shortValue(), type);
		assertEquals(response.getAccount().getAccountInfo().getCurrency(), currency);
		
		//create
		response = ResponseFactory.create(transactionID, gateID, sender, 
				status, error, AccountFactory.account(
						balance, type, accountNo, currency));
		assertEquals(response.getAccount(), AccountFactory.account(accountNo));
		assertEquals(response.getError(), error);
		assertEquals(response.getGateID().longValue(), gateID);
		assertEquals(response.getId(), transactionID);
		assertEquals(response.getSender(), sender);
		assertEquals(response.getStatus(), status);
		assertEquals(response.getResponseType(), MyResponse.RESPONSE_TYPE_CREATE_ACCOUNT);
		assertEquals(response.getAccount().getBalance().doubleValue(), balance, 0.001d);
		assertEquals(response.getAccount().getAccountInfo().getType().shortValue(), type);
		assertEquals(response.getAccount().getAccountInfo().getCurrency(), currency);
	}
	
	@Test
	public void testLogEntityFactory() {
		long transactionID = 1000l;
		String accountNo = AccountFactory.generateAccountNumber();
		String accountNo2 = AccountFactory.generateAccountNumber();
		long gateID = 1l;
		String sender = "sender";
		double amount = 10000d;
		String currency = "USD";
		short type = AccountInfo.TYPE_LOAD;
		boolean status = true;
		double balanceBefore = 1900d;
		double balanceAfter = 3400d;
		long timestamp = System.currentTimeMillis();
		double balance = 1000d;
		
		//withdrawal
		MyRequest request = RequestFactory.withdrawal(transactionID, gateID, sender, accountNo, amount);
		request.setTimestamp(timestamp);
		LogEntity log = LogEntityFactory.withdrawal(request, status, 
				balanceBefore, balanceAfter);
		assertEquals(log.getClass(), FinancialLogEntity.class);
		assertEquals(log.getAccountNo(), accountNo);
		assertEquals(((FinancialLogEntity)log).getBalanceAfter().doubleValue(), balanceAfter, 0.001d);
		assertEquals(((FinancialLogEntity)log).getBalanceBefore().doubleValue(), balanceBefore, 0.001d);
		assertEquals(log.getSender(), sender);
		assertEquals(log.getTransactionID().longValue(), transactionID);
		assertEquals(log.getTimestamp().longValue(), timestamp);
		assertEquals(log.getType(), LogEntity.TYPE_WITHDRAWAL);
		
		//deposit
		request = RequestFactory.deposit(transactionID, gateID, sender, accountNo, amount);
		request.setTimestamp(timestamp);
		log = LogEntityFactory.deposit(request, status, 
				balanceBefore, balanceAfter);
		assertEquals(log.getClass(), FinancialLogEntity.class);
		assertEquals(log.getAccountNo(), accountNo);
		assertEquals(((FinancialLogEntity)log).getBalanceAfter().doubleValue(), balanceAfter, 0.001d);
		assertEquals(((FinancialLogEntity)log).getBalanceBefore().doubleValue(), balanceBefore, 0.001d);
		assertEquals(log.getSender(), sender);
		assertEquals(log.getTransactionID().longValue(), transactionID);
		assertEquals(log.getTimestamp().longValue(), timestamp);
		assertEquals(log.getType(), LogEntity.TYPE_DEPOSIT);
		
		//transfer
		request = RequestFactory.transfer(transactionID, gateID, sender, accountNo, amount, accountNo2, balance);
		request.setTimestamp(timestamp);
		log = LogEntityFactory.transfer(request, status, 
				balanceBefore, balanceAfter, accountNo2);
		assertEquals(log.getClass(), TransferLogEntity.class);
		assertEquals(log.getAccountNo(), accountNo);
		assertEquals(((TransferLogEntity)log).getBalanceAfter().doubleValue(), balanceAfter, 0.001d);
		assertEquals(((TransferLogEntity)log).getBalanceBefore().doubleValue(), balanceBefore, 0.001d);
		assertEquals(((TransferLogEntity)log).getDestinationAccountNo(), accountNo2);
		assertEquals(log.getSender(), sender);
		assertEquals(log.getTransactionID().longValue(), transactionID);
		assertEquals(log.getTimestamp().longValue(), timestamp);
		assertEquals(log.getType(), LogEntity.TYPE_TRANSFER);
		
		//update
		Account accountBefore = AccountFactory.account(
				balanceBefore, type, accountNo, currency);
		request = RequestFactory.update(transactionID, gateID, sender, accountBefore);
		request.setTimestamp(timestamp);
		Account accountAfter = AccountFactory.account(
				balanceAfter, type, accountNo, currency);
		log = LogEntityFactory.update(request, status, accountBefore, accountAfter);
		assertEquals(log.getClass(), UpdateLogEntity.class);
		assertEquals(log.getAccountNo(), accountNo);
		assertEquals(((UpdateLogEntity)log).getAccountBefore(), accountBefore);
		assertEquals(((UpdateLogEntity)log).getAccountAfter(), accountAfter);
		assertEquals(log.getSender(), sender);
		assertEquals(log.getTransactionID().longValue(), transactionID);
		assertEquals(log.getTimestamp().longValue(), timestamp);
		assertEquals(log.getType(), LogEntity.TYPE_UPDATE_ACCOUNT);
		
		//create
		Account newAccount = AccountFactory.account(balanceBefore, type, accountNo, currency);
		request = RequestFactory.create(transactionID, gateID, sender, newAccount);
		request.setTimestamp(timestamp);
		log = LogEntityFactory.create(request, status, accountAfter);
		assertEquals(log.getClass(), UpdateLogEntity.class);
		assertEquals(log.getAccountNo(), accountNo);
		assertEquals(((UpdateLogEntity)log).getAccountBefore(), null);
		assertEquals(((UpdateLogEntity)log).getAccountAfter(), newAccount);
		assertEquals(log.getSender(), sender);
		assertEquals(log.getTransactionID().longValue(), transactionID);
		assertEquals(log.getTimestamp().longValue(), timestamp);
		assertEquals(log.getType(), LogEntity.TYPE_CREATE_ACCOUNT);
	}
	
}
