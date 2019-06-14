package com.mobilab.accountservice.bussiness.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * a class to represent configuration and facilitate some usual operations on converting string to primitives,
 * so every configuration value could be given by a specific data type
 */

public class Config {
	
	private static Properties props = new Properties();
	
	/*
	 * read properties on startup
	 */
	static {
		String resourceName = "application.properties";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
		    props.load(resourceStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * get all of properties or a subset of them starting with prefix
	 */
	public static Map<Object, Object> getConfig(String prefix){
		final Map<Object, Object> result = new HashMap<Object, Object>();
		final int prefixLength = (prefix==null ? 0 : prefix.length());
		props.entrySet().forEach(new Consumer<Entry<Object, Object>>() {
			@Override
			public void accept(Entry<Object, Object> t) {
				String k = t.getKey().toString();
				Object v = t.getValue();
				if(prefix==null || k.startsWith(prefix)) {
					result.put(k.substring(prefixLength), v);
				}
			}
		});
		return result;
	}
	
	/*
	 * get value as string if exists and is not empty, otherwise return a nullable def
	 */
	public static String getStringProperty(String key, String def) {
		String v = props.getProperty(key);
		return isNullOrEmpty(v) ? def : v;
	}
	
	/*
	 * get value as int if exists, otherwise return a nullable def
	 */
	public static Integer getIntProperty(String key, Integer def) {
		return toInt(props.getProperty(key), def);
	}
	
	/*
	 * get value as float if exists, otherwise return a nullable def
	 */
	public static Float getFloatProperty(String key, Float def) {
		return toFloat(props.getProperty(key), def);
	}
	
	/*
	 * get value as short if exists, otherwise return a nullable def
	 */
	public static Short getShortProperty(String key, Short def) {
		return toShort(props.getProperty(key), def);
	}
	
	/*
	 * get value as double if exists, otherwise return a nullable def
	 */
	public static Double getDoubleProperty(String key, Double def) {
		return toDouble(props.getProperty(key), def);
	}
	
	/*
	 * get value as long if exists, otherwise return a nullable def
	 */
	public static Long getLongProperty(String key, Long def) {
		return toLong(props.getProperty(key), def);
	}
	
	/*
	 * get value as boolean if exists, otherwise return a nullable def
	 */
	public static Boolean getBooleanProperty(String key, Boolean def) {
		return toBoolean(props.getProperty(key), def);
	}
	
	public static boolean isNullOrEmpty(String s) {
		return s==null || s.length()==0;
	}
	
	/*
	 * try to convert an Object (String) to int if is not null and is of kind int
	 */
	public static Integer toInt(Object o, Integer def) {
		if(o==null) {
			return def;
		}
		try {
			return Integer.parseInt(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}
	
	/*
	 * try to convert an Object (String) to float if is not null and is of kind float
	 */
	public static Float toFloat(Object o, Float def) {
		if(o==null) {
			return def;
		}
		try {
			return Float.parseFloat(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}
	
	/*
	 * try to convert an Object (String) to double if is not null and is of kind double
	 */
	public static Double toDouble(Object o, Double def) {
		if(o==null) {
			return def;
		}
		try {
			return Double.parseDouble(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}
	
	/*
	 * try to convert an Object (String) to short if is not null and is of kind short
	 */
	public static Short toShort(Object o, Short def) {
		if(o==null) {
			return def;
		}
		try {
			return Short.parseShort(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}
	
	/*
	 * try to convert an Object (String) to long if is not null and is of kind long
	 */
	public static Long toLong(Object o, Long def) {
		if(o==null) {
			return def;
		}
		try {
			return Long.parseLong(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}
	
	/*
	 * try to convert an Object (String) to Boolean if is not null and is of kind boolean
	 */
	public static Boolean toBoolean(Object o, Boolean def) {
		if(o==null) {
			return def;
		}
		try {
			return Boolean.parseBoolean(o.toString());
		}
		catch(Exception ex) {
			return def;
		}
	}

	public static String getNotNull(String s) {
		return s==null ? "" : s;
	}
	
	public static short getNotNull(Short s) {
		return s==null ? (short)0 : s;
	}
	
	public static double getNotNull(Double d) {
		return d==null ? 0d : d;
	}
	
	public static Object getProperty(String key) {
		return props.get(key);
	}
	
	public static void setProperty(String key, String value) {
		props.put(key, value);
	}

}
