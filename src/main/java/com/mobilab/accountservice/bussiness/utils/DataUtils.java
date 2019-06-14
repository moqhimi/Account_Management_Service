package com.mobilab.accountservice.bussiness.utils;

import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.PersonalInfo;

/**
 * general purpose utility to perform some operations on data types and Account objects
 */
public class DataUtils {
	
	
	public static boolean accountsAreEqual(Account a, Account b) {
		return accountInfoAreEqual(a.getAccountInfo(), b.getAccountInfo()) && 
				personalInfoAreEqual(a.getPersonalInfo(), b.getPersonalInfo());
	}
	
	public static boolean accountInfoAreEqual(AccountInfo a, AccountInfo b) {
		return areNullOrEqual(a.getCurrency(), b.getCurrency()) && 
				areNullOrEqual(a.getType(), b.getType());
	}
	
	public static boolean personalInfoAreEqual(PersonalInfo a, PersonalInfo b) {
		return areNullOrEqual(a.getFirstname(), b.getFirstname()) && 
				areNullOrEqual(a.getLastname(), b.getLastname()) && 
				areNullOrEqual(a.getAddress(), b.getAddress()) && 
				areNullOrEqual(a.getBirthdate(), b.getBirthdate()) && 
				areNullOrEqual(a.getEmail(), b.getEmail()) && 
				areNullOrEqual(a.getMobileNumber(), b.getMobileNumber()) && 
				areNullOrEqual(a.getPassportNo(), b.getPassportNo()) && 
				areNullOrEqual(a.getPhoneNumber(), b.getPhoneNumber());
	}
	
	public static boolean areNullOrEqual(String o1, String o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.compareTo(o2)==0;
	}
	
	public static boolean areNullOrEqual(Short o1, Short o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.shortValue()==o2.shortValue();
	}
	
	public static boolean areNullOrEqual(Integer o1, Integer o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.intValue()==o2.intValue();
	}
	
	public static boolean areNullOrEqual(Float o1, Float o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.floatValue()==o2.floatValue();
	}
	
	public static boolean areNullOrEqual(Double o1, Double o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.doubleValue()==o2.doubleValue();
	}
	
	public static boolean areNullOrEqual(Long o1, Long o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.longValue()==o2.longValue();
	}
	
	public static boolean areNullOrEqual(Boolean o1, Boolean o2) {
		if(o1==null) {
			return o2==null;
		}
		if(o2==null) {
			return false;
		}
		return o1.booleanValue()==o2.booleanValue();
	}
	
}
