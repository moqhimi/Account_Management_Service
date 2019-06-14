package com.mobilab.accountservice.testcase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.ActionTransaction;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ConsistencyTest extends BaseTest {
	
	private double balance = 10000;
	
	private int count;
	
	public ConsistencyTest() {
		this.count = 10;
		this.deleteTestData();
	}
	
	public void run() {
		final Queue<String> tidList = new LinkedList<String>();
		final List<Account> createdAccounts = new ArrayList<Account>();
		final List<Account> updatedAccounts = new ArrayList<Account>();

		// cerate accounts
		for(int i=0; i<count; i++) {
			String transactionId = createNewAccount(balance);
			tidList.add(transactionId);
			System.out.println("<<<<<< Send Create Request - transactionId = " + transactionId);
			try {
				Thread.sleep(1);
			} catch (Exception ex) {}
		}
		
		try {
			Thread.sleep(1000);
		} catch (Exception ex) {}
		
		//read created accounts
		while(tidList.size()>0){
			String transactionId = tidList.remove();
			Account createdAccount = getTransactionResult(transactionId);
			if(createdAccount==null) {
				tidList.add(transactionId);
			}
			else {
				System.out.println("Created Account - No = "+createdAccount.getAccountInfo().getAccountNo()+
						" , Balance = "+createdAccount.getBalance());
				createdAccounts.add(createdAccount);
			}
			try {
				Thread.sleep(10);
			} catch (Exception ex) {}
		}
		
		double amount = 1000;
		
		tidList.clear();
		
		//transfer
		for(int i=0; i< count; i++) {
			String sourceAccountNo = createdAccounts.get(i).getAccountInfo().getAccountNo();
			String destAccountNo = createdAccounts.get((i+1) % count).getAccountInfo().getAccountNo();
			System.out.println("Transfer "+sourceAccountNo+" => "+destAccountNo+" - "+amount+"$");
			String transactionID = this.transferRequest(sourceAccountNo, destAccountNo, amount);
			tidList.add(transactionID);
		}
		
		//read updated balance
		while(tidList.size()>0){
			String transactionId = tidList.remove();
			Account updatedAccount = getTransactionResult(transactionId);
			if(updatedAccount==null) {
				tidList.add(transactionId);
			}
			else {
				System.out.println("Updated Account - No = "+updatedAccount.getAccountInfo().getAccountNo()+
						" , Balance = "+updatedAccount.getBalance());
				updatedAccounts.add(updatedAccount);
			}
			try {
				Thread.sleep(10);
			} catch (Exception ex) {}
		}
		
		for(Account updated : updatedAccounts) {
			Account before = createdAccounts.get(createdAccounts.indexOf(updated));
			double balanceBefore = before.getBalance();
			double balanceAfter = updated.getBalance();
			Assert.isTrue(balanceAfter == balanceBefore);
		}
		
		this.deleteTestData();
	}
	
	private String transferRequest(String sourceAccountNo, String destAccountNo, double amount) {
		try {
			final ActionTransaction action = new ActionTransaction("TRANSFER", destAccountNo, amount);
			final Response response = RestAssured.given().contentType(
					MediaType.APPLICATION_JSON_VALUE).body(action)
					.post(API_ROOT + "accounts/"+sourceAccountNo);
			return response.jsonPath().get("transaction_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	
}
