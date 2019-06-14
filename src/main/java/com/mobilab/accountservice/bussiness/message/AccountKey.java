package com.mobilab.accountservice.bussiness.message;

/**
 * represents account structure to generate unique key for every request.
 * it is used in partitioning in Kafka and also is used in list , map operations such as indexOf, contains, put
 */

public class AccountKey {

	private String accountNo;
		
	public AccountKey(String accountNo) {
		this.accountNo = accountNo;
	}

	/**
	 * generate a unique hashCode by accountNo.
	 * the result is positive due to partition keys in Kafka.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNo == null) ? 0 : accountNo.hashCode());
		return Math.abs(result);
	}
	
}
