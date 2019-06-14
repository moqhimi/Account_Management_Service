package com.mobilab.accountservice.bussiness.CurrencyExchage;

import com.google.gson.Gson;
import com.mobilab.accountservice.service.TransactionResultService;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExchangeWebService {
	final static Logger logger = Logger.getLogger(TransactionResultService.class);
	private static Exchangerates rs = null;
	private static final Gson GSON = new Gson();
	private final static String OPEN_EXCHANGE_RATE_SERVICE_URL="https://openexchangerates.org/api/latest.json?app_id=e4abf9924e014610906a73cb9957bb46";
	private static void refreshRates() {
		try {
			if (rs == null || rs.isExpired()) {
				String jsonString = getConnection(new URL(OPEN_EXCHANGE_RATE_SERVICE_URL), null);
				logger.debug("refreshRates jsonString " + jsonString);
				rs = GSON.fromJson(jsonString, Exchangerates.class);
				logger.debug("refreshRates rs " + rs.toString());
				if (rs == null || rs.isExpired()) {
					throw new Exception();
				}
			}
		} catch (Exception ex) {
			logger.error("refreshRates error: ", ex);
		}
	}
	public static Double getWebRate(String fromcurrency, String tocurrency) {
		if(fromcurrency==null || tocurrency==null || 
				fromcurrency.toLowerCase().compareTo(tocurrency.toLowerCase())==0) {
			return 1d;
		}
		refreshRates();
		Double torate = rs.getRate(tocurrency);
		if ("USD".equalsIgnoreCase(fromcurrency)) {
			return torate;
		} else {
			Double fromrate = rs.getRate(fromcurrency);
			return (torate == null || fromrate == null || fromrate == 0.0) ? 0.0 : torate / fromrate;
		}
	}

	private static String getConnection(URL url, String rq) throws Exception {
		StringBuilder jsonString = new StringBuilder();
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");

			if (rq != null) {
				connection.setRequestProperty("Accept", "application/json");
				connection.connect();
				byte[] outputBytes = rq.getBytes("UTF-8");
				OutputStream os = connection.getOutputStream();
				os.write(outputBytes);
			}

			if (connection.getResponseCode() != 200) {
				throw new RuntimeException("HTTP:" + connection.getResponseCode() + "URL " + url);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			String line;
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
		} catch (Exception x) {
			throw new RuntimeException(x.getMessage());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return jsonString.toString();
	}
}
