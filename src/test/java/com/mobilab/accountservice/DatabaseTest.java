package com.mobilab.accountservice;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.CurrencyMap;
import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.RequestFactory;
import com.mobilab.accountservice.bussiness.process.Database;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.FinancialLogEntity;
import com.mobilab.accountservice.entities.LogEntity;
import com.mobilab.accountservice.entities.LogEntityFactory;
import com.mobilab.accountservice.entities.UpdateLogEntity;
import com.mongodb.BasicDBObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

public class DatabaseTest {

	private final String TEST_PREFIX = "test_account_";

	private Database db;

	private short[] accountTypeArray = new short[] { AccountInfo.TYPE_LOAD, AccountInfo.TYPE_SAVINGS };
	private String[] currencyArray = new String[] { "EUR", "USD" };
	
	@Before
	public void before() {
		// connect before start test
		if(db==null) {
			db = new Database();
		}
		db.connect();
		deleteTestData();
	}

	@After
	public void after() {
		//delete all test data before termination
		deleteTestData();
		// close connection after test get completed
		db.close();
	}
	
	private void deleteTestData() {
		// delete accounts and logs that their accountNo starts with test
		Pattern pattern = Pattern.compile("^" + Pattern.quote(TEST_PREFIX), Pattern.CASE_INSENSITIVE);
		db.getAccountCollection().deleteMany(new BasicDBObject().append("accountInfo.accountNo", pattern));
		db.getLogCollection().deleteMany(new BasicDBObject().append("account", pattern));
	}

	@Test
	public void testConnection() {
		assertNotNull(db.getAccountCollection());
		assertNotNull(db.getLogCollection());
		assertNotNull(db.getCollection());
		assertNotNull(db.getDb());
		assertNotNull(db.getMongoClient());
	}

	@Test
	public void testLogJsonMapper() {
		FinancialLogEntity log = LogEntityFactory.withdrawal(
				RequestFactory.withdrawal(100000l, 999l, "sender", AccountFactory.generateAccountNumber(), 100d), true,
				1000d, 100d);
		StringWriter out = new StringWriter();
		String json = null;
		try {
			db.getJsonMapper().writeValue(out, log);
			json = out.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		assertNotNull(json);
		FinancialLogEntity log2 = null;
		try {
			log2 = db.getJsonMapper().readValue(json, FinancialLogEntity.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		assertNotNull(log2);
		assertEquals(log.getAccountNo(), log2.getAccountNo());
		assertEquals(log.getBalanceAfter(), log2.getBalanceAfter());
		assertEquals(log.getBalanceBefore(), log2.getBalanceBefore());
		assertEquals(log.getSender(), log2.getSender());
		assertEquals(log.getTimestamp(), log2.getTimestamp());
		assertEquals(log.getTransactionID(), log2.getTransactionID());
		assertEquals(log.getType(), log2.getType());
	}
	
	@Test
	public void testCreateAndReadAccount() {
		final int count = 10;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Long accountID = (long) i;
			Double balance = i * 1000d;
			String firstname = "f" + i;
			String lastname = "l" + i;
			String birthdate = "b" + i;
			String passportNo = "p" + i;
			String address = "a" + i;
			String email = "e" + i;
			String phoneNumber = "ph" + i;
			String mobileNumber = "mo" + i;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			String createdAt = "created" + i;
			Account account = AccountFactory.account(accountID, balance, firstname, lastname, birthdate, passportNo,
					address, email, phoneNumber, mobileNumber, type, accountNo, currency, createdAt);
			accountList.add(account);
			try {
				db.createAccount(account);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//read
		for(i=0; i<count; i++) {
			final Account orig = accountList.get(i);
			Account account = null;
			try {
				account = db.readAccount(AccountFactory.account(TEST_PREFIX+i));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			assertNotNull(account);
			assertEquals(account, orig);
			assertEquals(account.getAccountID(), orig.getAccountID());
			assertEquals(account.getBalance(), orig.getBalance());
			assertEquals(account.getPersonalInfo().getAddress(), 
					orig.getPersonalInfo().getAddress());
			assertEquals(account.getPersonalInfo().getBirthdate(), 
					orig.getPersonalInfo().getBirthdate());
			assertEquals(account.getPersonalInfo().getEmail(), 
					orig.getPersonalInfo().getEmail());
			assertEquals(account.getPersonalInfo().getFirstname(), 
					orig.getPersonalInfo().getFirstname());
			assertEquals(account.getPersonalInfo().getLastname(), 
					orig.getPersonalInfo().getLastname());
			assertEquals(account.getPersonalInfo().getMobileNumber(), 
					orig.getPersonalInfo().getMobileNumber());
			assertEquals(account.getPersonalInfo().getPassportNo(), 
					orig.getPersonalInfo().getPassportNo());
			assertEquals(account.getPersonalInfo().getPhoneNumber(), 
					orig.getPersonalInfo().getPhoneNumber());
			assertEquals(account.getAccountInfo().getAccountNo(), 
					orig.getAccountInfo().getAccountNo());
			assertEquals(account.getAccountInfo().getCreatedAt(), 
					orig.getAccountInfo().getCreatedAt());
			assertEquals(account.getAccountInfo().getCurrency(), 
					orig.getAccountInfo().getCurrency());
			assertEquals(account.getAccountInfo().getType(), 
					orig.getAccountInfo().getType());
		}
		
		//batch read
		i = 0;
		for(Account account : db.batchRead(accountList)) {
			final Account orig = accountList.get(i++);
			assertNotNull(account);
			assertEquals(account, orig);
			assertEquals(account.getAccountID(), orig.getAccountID());
			assertEquals(account.getBalance(), orig.getBalance());
			assertEquals(account.getPersonalInfo().getAddress(), 
					orig.getPersonalInfo().getAddress());
			assertEquals(account.getPersonalInfo().getBirthdate(), 
					orig.getPersonalInfo().getBirthdate());
			assertEquals(account.getPersonalInfo().getEmail(), 
					orig.getPersonalInfo().getEmail());
			assertEquals(account.getPersonalInfo().getFirstname(), 
					orig.getPersonalInfo().getFirstname());
			assertEquals(account.getPersonalInfo().getLastname(), 
					orig.getPersonalInfo().getLastname());
			assertEquals(account.getPersonalInfo().getMobileNumber(), 
					orig.getPersonalInfo().getMobileNumber());
			assertEquals(account.getPersonalInfo().getPassportNo(), 
					orig.getPersonalInfo().getPassportNo());
			assertEquals(account.getPersonalInfo().getPhoneNumber(), 
					orig.getPersonalInfo().getPhoneNumber());
			assertEquals(account.getAccountInfo().getAccountNo(), 
					orig.getAccountInfo().getAccountNo());
			assertEquals(account.getAccountInfo().getCreatedAt(), 
					orig.getAccountInfo().getCreatedAt());
			assertEquals(account.getAccountInfo().getCurrency(), 
					orig.getAccountInfo().getCurrency());
			assertEquals(account.getAccountInfo().getType(), 
					orig.getAccountInfo().getType());
		}
		
	}
	
	@Test
	public void testUpdateAll() {
		final int count = 10;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Long accountID = (long) i;
			Double balance = i * 1000d;
			String firstname = "f" + i;
			String lastname = "l" + i;
			String birthdate = "b" + i;
			String passportNo = "p" + i;
			String address = "a" + i;
			String email = "e" + i;
			String phoneNumber = "ph" + i;
			String mobileNumber = "mo" + i;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			String createdAt = "created" + i;
			Account account = AccountFactory.account(accountID, balance, firstname, lastname, birthdate, passportNo,
					address, email, phoneNumber, mobileNumber, type, accountNo, currency, createdAt);
			accountList.add(account);
		}
		
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//update
		Map<MyRequest, CurrencyMap> list = new HashMap<MyRequest, CurrencyMap>();
		i = 0;
		//contains what accounts should turned into in
		List<Account> updatedListOrig = new ArrayList<Account>();
		for(Account account : accountList) {
			Double balance = account.getBalance() + 999d;
			String firstname = account.getPersonalInfo().getFirstname() + "_2";
			String lastname = account.getPersonalInfo().getLastname() + "_2";
			String birthdate = account.getPersonalInfo().getBirthdate() + "_2";
			String passportNo = account.getPersonalInfo().getPassportNo() + "_2";
			String address = account.getPersonalInfo().getAddress() + "_2";
			String email = account.getPersonalInfo().getEmail() + "_2";
			String phoneNumber = account.getPersonalInfo().getPhoneNumber() + "_2";
			String mobileNumber = account.getPersonalInfo().getMobileNumber() + "_2";
			Short type = accountTypeArray[(i+1) % accountTypeArray.length];
			String currency = currencyArray[(i+1) % currencyArray.length];
			String createdAt = "created" + i + "_2";
			account.setBalance(balance);
			account.getPersonalInfo().setFirstname(firstname);
			account.getPersonalInfo().setLastname(lastname);
			account.getPersonalInfo().setBirthdate(birthdate);
			account.getPersonalInfo().setPassportNo(passportNo);
			account.getPersonalInfo().setAddress(address);
			account.getPersonalInfo().setEmail(email);
			account.getPersonalInfo().setPhoneNumber(phoneNumber);
			account.getPersonalInfo().setMobileNumber(mobileNumber);
			account.getAccountInfo().setType(type);
			account.getAccountInfo().setCurrency(currency);
			account.getAccountInfo().setCreatedAt(createdAt);
			updatedListOrig.add(account);
			list.put(RequestFactory.update(1000l + i, 100l, "sender", account), null);
		}
		List<Account> updatedList = db.batchUpdate(list);
		//compare updatedList with updatedListOrig
		i = 0;
		for(Account updated : updatedList) {
			final Account shouldBe = accountList.get(i++);
			assertNotNull(updated);
			assertEquals(updated, shouldBe);
			assertEquals(updated.getAccountID(), shouldBe.getAccountID());
			assertEquals(updated.getBalance(), shouldBe.getBalance());
			assertEquals(updated.getPersonalInfo().getAddress(), 
					shouldBe.getPersonalInfo().getAddress());
			assertEquals(updated.getPersonalInfo().getBirthdate(), 
					shouldBe.getPersonalInfo().getBirthdate());
			assertEquals(updated.getPersonalInfo().getEmail(), 
					shouldBe.getPersonalInfo().getEmail());
			assertEquals(updated.getPersonalInfo().getFirstname(), 
					shouldBe.getPersonalInfo().getFirstname());
			assertEquals(updated.getPersonalInfo().getLastname(), 
					shouldBe.getPersonalInfo().getLastname());
			assertEquals(updated.getPersonalInfo().getMobileNumber(), 
					shouldBe.getPersonalInfo().getMobileNumber());
			assertEquals(updated.getPersonalInfo().getPassportNo(), 
					shouldBe.getPersonalInfo().getPassportNo());
			assertEquals(updated.getPersonalInfo().getPhoneNumber(), 
					shouldBe.getPersonalInfo().getPhoneNumber());
			assertEquals(updated.getAccountInfo().getAccountNo(), 
					shouldBe.getAccountInfo().getAccountNo());
			assertEquals(updated.getAccountInfo().getCreatedAt(), 
					shouldBe.getAccountInfo().getCreatedAt());
			assertEquals(updated.getAccountInfo().getCurrency(), 
					shouldBe.getAccountInfo().getCurrency());
			assertEquals(updated.getAccountInfo().getType(), 
					shouldBe.getAccountInfo().getType());
		}
	}
	
	@Test
	public void testWithdrawal() {
		final int count = 10;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Double balance = (i+1) * 1000d;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			Account account = AccountFactory.account(balance, type, accountNo, currency);
			accountList.add(account);
		}
		
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//update
		Map<MyRequest, CurrencyMap> list = new HashMap<MyRequest, CurrencyMap>();
		i = 0;
		//contains what accounts should turned into in
		List<Account> updatedListOrig = new ArrayList<Account>();
		for(Account account : accountList) {
			double amount = 3001;
			//account 0 , 1 , 2 do not have enough balance for withdrawal
			Double newBalance = account.getBalance() > amount ? (account.getBalance() - amount)
					: account.getBalance();
			account.setBalance(newBalance);
			updatedListOrig.add(account);
			list.put(RequestFactory.withdrawal(1000l + i, 100l, "sender", 
					account.getAccountInfo().getAccountNo(), amount), null);
			i++;
		}
		List<Account> updatedList = db.batchUpdate(list);
		//compare updatedList with updatedListOrig
		i = 0;
		for(Account updated : updatedList) {
			final Account shouldBe = accountList.get(i);
			assertNotNull(updated);
			assertEquals(updated, shouldBe);
			assertEquals(updated.getBalance(), shouldBe.getBalance());
			++i;
		}
	}
	
	@Test
	public void testDeposit() {
		final int count = 10;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Double balance = (i+1) * 1000d;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			Account account = AccountFactory.account(balance, type, accountNo, currency);
			accountList.add(account);
		}
		
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//update
		Map<MyRequest, CurrencyMap> list = new HashMap<MyRequest, CurrencyMap>();
		i = 0;
		//contains what accounts should turned into in
		List<Account> updatedListOrig = new ArrayList<Account>();
		for(Account account : accountList) {
			double amount = 100d * (i+1);
			Double newBalance = account.getBalance() + amount;
			account.setBalance(newBalance);
			updatedListOrig.add(account);
			list.put(RequestFactory.deposit(1000l + i, 100l, "sender", 
					account.getAccountInfo().getAccountNo(), amount), null);
			i++;
		}
		List<Account> updatedList = db.batchUpdate(list);
		//compare updatedList with updatedListOrig
		i = 0;
		for(Account updated : updatedList) {
			final Account shouldBe = accountList.get(i);
			assertNotNull(updated);
			assertEquals(updated, shouldBe);
			assertEquals(updated.getBalance(), shouldBe.getBalance());
			++i;
		}
	}
	
	@Test
	public void testTransfer() {
		final int count = 10;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;
		final String[] currencyArray2 = new String[] {"USD" , "EUR" , "USD" , "EUR" , "USD" , 
				"EUR" , "USD" , "EUR" , "EUR" , "USD"};
		//create
		for (i = 0; i < count; i++) {
			Double balance = (i+1) * 1000d;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray2[i % currencyArray2.length];
			Account account = AccountFactory.account(balance, type, accountNo, currency);
			accountList.add(account);
		}
		
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//update
		Map<MyRequest, CurrencyMap> list = new HashMap<MyRequest, CurrencyMap>();
		i = 0;
		//contains what accounts should turned into in
		Account[] updatedListOrig = new Account[count];
		/*
		 * 0 => 5 : USD => EUR
		 * 1 => 6 : EUR => USD
		 * 2 => 7 : USD => EUR
		 * 3 => 8 : EUR => EUR
		 * 4 => 9 : USD => USD
		 */
		/*
		 * assumption:
		 * each dollar is 0.8 of an euro
		 */
		for(i=0; i<5; i++) {
			Account account1 = accountList.get(i);
			double amount = 1001;
			//account 0 does not have enough balance to transfer
			double oldBalance = account1.getBalance().doubleValue();
			Double newBalance = oldBalance > amount ? (oldBalance - amount) : oldBalance;
			account1.setBalance(newBalance);
			updatedListOrig[i] = account1;
			
			Account account2 = accountList.get(i+5);
			String currency1 = account1.getAccountInfo().getCurrency();
			String currency2 = account2.getAccountInfo().getCurrency();
			CurrencyMap currencyMap = new CurrencyMap(currency1, currency2, 
					getCurrencyRate(currency1, currency2));
			newBalance = oldBalance > amount ? (account2.getBalance() + amount * currencyMap.getRate())
					: account2.getBalance();
			account2.setBalance(newBalance);
			updatedListOrig[i+5] = account2;
			
			MyRequest request = RequestFactory.transfer(1000l + i, 100l, "sender", 
					account1.getAccountInfo().getAccountNo(), amount, 
					account2.getAccountInfo().getAccountNo(), oldBalance);
			list.put(request, currencyMap);
		}
		List<Account> updatedList = null;
		try{
			updatedList = db.batchUpdate(list);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		assertNotNull(updatedList);
		//compare updatedList with updatedListOrig
		i = 0;
		for(Account updated : updatedList) {
			final Account shouldBe = accountList.get(i);
			assertNotNull(updated);
			assertEquals(updated, shouldBe);
			assertEquals(updated.getBalance(), shouldBe.getBalance());
			++i;
		}
		
		//test balance for all accounts
		try{
			updatedList = db.batchRead(accountList);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		assertNotNull(updatedList);
		//compare updatedList with updatedListOrig
		i = 0;
		for(Account updated : updatedList) {
			final Account shouldBe = accountList.get(i);
			assertNotNull(updated);
			assertEquals(updated, shouldBe);
			assertEquals(updated.getBalance(), shouldBe.getBalance());
			++i;
		}
	}
	
	private double getCurrencyRate(String currency1, String currency2) {
		currency1 = currency1.toLowerCase();
		currency2 = currency2.toLowerCase();
		if(currency1.compareTo(currency2)==0) {
			return 1;
		}
		if(currency1.compareTo("usd")==0) {
			return 0.8d;
		}
		return 1 / 0.8d;
	}
	
	@Test
	public void testLog() {
		final int count = 10;
		final Map<Long, LogEntity> logs = new HashMap<Long, LogEntity>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Double balance = (i+1) * 1000d;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			Account account = AccountFactory.account(balance, type, accountNo, currency);
			long requestID = 1000l + i;
			logs.put(requestID, LogEntityFactory.create(RequestFactory.create(
					requestID, 100l, "sender", account), true, account));
		}
		
		try {
			db.insertLog(logs.values());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//search by accountNo
		final String accountNo = TEST_PREFIX+"1";
		db.searchLog(true, null, null, null, accountNo, new Consumer<LogEntity>() {
			@Override
			public void accept(LogEntity log) {
				assertEquals(log.getAccountNo(), accountNo);
			}
		});
	}
	
	@Test
	public void benchmarkCreateRead() {
		int count = 100;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;
				
		//create
		long t1 = System.currentTimeMillis();
		for (i = 0; i < count; i++) {
			Long accountID = (long) i;
			Double balance = i * 1000d;
			String firstname = "f" + i;
			String lastname = "l" + i;
			String birthdate = "b" + i;
			String passportNo = "p" + i;
			String address = "a" + i;
			String email = "e" + i;
			String phoneNumber = "ph" + i;
			String mobileNumber = "mo" + i;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			String createdAt = "created" + i;
			Account account = AccountFactory.account(accountID, balance, firstname, lastname, birthdate, passportNo,
					address, email, phoneNumber, mobileNumber, type, accountNo, currency, createdAt);
			accountList.add(account);
			try {
				db.createAccount(account);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//read
		long t2 = System.currentTimeMillis();
		for(i=0; i<count; i++) {
			try {
				db.readAccount(AccountFactory.account(TEST_PREFIX+i));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		//batch read
		long t3 = System.currentTimeMillis();
		db.batchRead(accountList);
		long t4 = System.currentTimeMillis();
		
		deleteTestData();
		
		//batch create
		long t5 = System.currentTimeMillis();
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t6 = System.currentTimeMillis();
		
		print(count+" accounts - Creation time = " + (t2 - t1) + " - Read time = " + (t3 - t2) + 
				" - Batch Insert time = " + (t6 - t5) + " - Batch Read time = " + (t4 - t3));
	}
	
	@Test
	public void benchmarkBatchUpdate() {
		final int count = 100;
		final List<Account> accountList = new ArrayList<Account>();
		int i = 0;

		//create
		for (i = 0; i < count; i++) {
			Double balance = (i+1) * 1000d;
			Short type = accountTypeArray[i % accountTypeArray.length];
			String accountNo = TEST_PREFIX + i;
			String currency = currencyArray[i % currencyArray.length];
			Account account = AccountFactory.account(balance, type, accountNo, currency);
			accountList.add(account);
		}
		
		long t1 = System.currentTimeMillis();
		try {
			db.batchInsert(accountList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long t2 = System.currentTimeMillis();
		
		//update
		Map<MyRequest, CurrencyMap> list = new HashMap<MyRequest, CurrencyMap>();
		i = 0;
		List<Account> updatedListOrig = new ArrayList<Account>();
		for(Account account : accountList) {
			double amount = 3001;
			Double newBalance = account.getBalance() > amount ? (account.getBalance() - amount)
					: account.getBalance();
			account.setBalance(newBalance);
			updatedListOrig.add(account);
			list.put(RequestFactory.withdrawal(1000l + i, 100l, "sender", 
					account.getAccountInfo().getAccountNo(), amount), null);
			i++;
		}
		long t3 = System.currentTimeMillis();
		db.batchUpdate(list);
		long t4 = System.currentTimeMillis();
		
		print(count+" accounts - Creation time = " + (t2 - t1) + 
				" - Batch Update time = " + (t4 - t3));
	}
	
	private void print(String msg) {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println(msg);
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}
	
}
