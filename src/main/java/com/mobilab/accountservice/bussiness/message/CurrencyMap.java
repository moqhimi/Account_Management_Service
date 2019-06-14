package com.mobilab.accountservice.bussiness.message;

/**
 * keep currency fields required in processing transfer requests
 */

public class CurrencyMap {
	
	/**
	 * the currency of source account in transfer request
	 */
	private String currencyFrom;
	
	/**
	 * the currency of destination account in transfer request
	 */
	private String currencyTo;
	
	/**
	 * value of the source currency divided by value of the destination currency 
	 */
	private double rate;
	
	public CurrencyMap() {
		
	}

	public CurrencyMap(String currencyFrom, String currencyTo, double rate) {
		this.currencyFrom = currencyFrom;
		this.currencyTo = currencyTo;
		this.rate = rate;
	}

	public String getCurrencyFrom() {
		return currencyFrom;
	}

	public void setCurrencyFrom(String currencyFrom) {
		this.currencyFrom = currencyFrom;
	}

	public String getCurrencyTo() {
		return currencyTo;
	}

	public void setCurrencyTo(String currencyTo) {
		this.currencyTo = currencyTo;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	@Override
	public String toString() {
		return "CurrencyMap [currencyFrom=" + currencyFrom + ", currencyTo=" + currencyTo + ", rate=" + rate + "]";
	}


	
}
