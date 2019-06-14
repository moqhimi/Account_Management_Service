package com.mobilab.accountservice.bussiness.CurrencyExchage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Exchangerates {
	private String disclaimer;
	private String license;
	private Long timestamp;
	private String base;
	private Rates rates;

	public Date getDate() {
		return new Date(timestamp * 1000);
	}

	public boolean isExpired() {
		//refresh daily on demand
		return getDate().before(plusXDay(new Date(), -1));
	}

	public HashMap<String, Double> getRates() {
		return rates.getRates();
	}
	
	public Double getRate(String currency) {
		return rates.getRate(currency);
	}


	private Date plusXDay(Date date, Integer x){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -1);
		return c.getTime();

	}

	@Override public String toString() {
		return "Exchangerates{" + "disclaimer='" + disclaimer + '\'' + ", license='" + license + '\'' + ", timestamp=" + timestamp + ", base='" + base + '\''
				+ ", rates=" + rates + '}';
	}


}