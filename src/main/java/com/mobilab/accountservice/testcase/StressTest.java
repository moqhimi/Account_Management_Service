package com.mobilab.accountservice.testcase;

import com.mobilab.accountservice.entities.Account;

import java.util.LinkedList;
import java.util.Queue;

/**
 * send many of create account requests and try to get inserted accounts.
 * it evaluates the time consumed for this and count the number of failed requests to get the response.
 * the sleep value is dynamically set according to the rate of successful responses. 
 */

public class StressTest extends BaseTest {
	
	private int count = 100;
	
	public StressTest(int count) {
		this.count = count;
		this.deleteTestData();
		System.out.println("Start Stress Test , count = "+count);
	}

	public void run() {
		final Queue<String> tidList = new LinkedList<String>();
		final Long[] time = new Long[4];
		// sending create account requests every 1 milliseconds
		new Thread(new Runnable() {
			public void run() {
				time[0] = System.currentTimeMillis();
				int i = 0;
				while (i < count) {
					i++;
					String transactionId = createNewAccount(null);
					System.out.println("<<<<<< Send Request - transactionId = " + transactionId);
					synchronized (tidList) {
						tidList.add(transactionId);
					}
					try {
						Thread.sleep(1);
					} catch (Exception ex) {}
				}
				time[1] = System.currentTimeMillis();
			}
		}).start();
		
		// sending requests to get created accounts.
		// set the sleep dynamically.
		new Thread(new Runnable() {
			public void run() {
				// wait until there is no request
				while (tidList.size() < 1) {
					try {
						Thread.sleep(100);
					} catch (Exception ex) {}
				}
				int sleep = 10;
				time[2] = System.currentTimeMillis();
				int sentCount = 0;
				while (tidList.size() > 0 || time[1]==null) {
					if (tidList.size() > 0) {
						String transactionId = null;
						synchronized (tidList) {
							transactionId = tidList.remove();	
						}
						if (transactionId != null) {
							++sentCount;
							Account returnedAccount = getTransactionResult(transactionId);
							System.out.println(">>>>>> Get Result = " + transactionId+
									" , returnedAccount = "+returnedAccount);
							// add transactionID to list if result is null.
							// it means that the requests is not handled by business node yet.
							if(returnedAccount==null) {
								tidList.add(transactionId);
								++sleep;
							}
							else {
								--sleep;
								if(sleep < 1) {
									sleep = 1;
								}
							}
						}
					}
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {}
				}
				time[3] = System.currentTimeMillis();
				System.out.println("count = "+count+" , time for sending requests = "+
						(time[1] - time[0])+" , time for getting responses = "+(time[3] - time[2])+
						" , sent requests to get response = "+sentCount+" , sleep = "+sleep);
				deleteTestData();
			}
		}).start();
	}

}
