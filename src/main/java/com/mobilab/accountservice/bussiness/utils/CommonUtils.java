package com.mobilab.accountservice.bussiness.utils;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
	final static Logger logger = Logger.getLogger(CommonUtils.class);

	private static final DateFormat DATE_FORMAT=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	public static Date parseDateString(String dateInString){
		Date date= null;
		try {
			date=DATE_FORMAT.parse(dateInString);
		} catch (ParseException e) {
			logger.error("Error in parsing string as date: "+e.getMessage());
		}
		return date;
	}

}
