package com.mobilab.accountservice.bussiness.utils;

public class NodeUtils {
	
	public static Long createID() {
		return (long) (Math.random() * 10000000l);
	}
	
}
