package com.mobilab.accountservice.testcase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.mobilab.accountservice.bussiness.process.Database;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.ApiResponseWithData;
import com.mobilab.accountservice.entities.MobiLabEnums;
import com.mobilab.accountservice.entities.PersonalInfo;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class BaseTest {
	
	protected static final String API_ROOT = "http://127.0.0.1:8080/api/v1/";
	protected static final int RANGE = 1000;
	protected static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss:SSS E");

	protected Account createRandomAccount(Double balance) {
		final Account account = new Account();
		final AccountInfo accountInfo = new AccountInfo();
		final PersonalInfo personalInfo = new PersonalInfo();
		Random r = new Random();
		Date date = new Date((long) (r.nextInt(1000000) * (System.currentTimeMillis())));
		accountInfo.setCurrency(MobiLabEnums.Currency.EUR.getStringValue());
		accountInfo.setCreatedAt(df.format(date));
		personalInfo.setFirstname(RandomStringUtils.randomAlphabetic(5));
		personalInfo.setLastname(RandomStringUtils.randomAlphabetic(5));
		personalInfo.setPassportNo(RandomStringUtils.randomAlphabetic(8));
		personalInfo.setMobileNumber(RandomStringUtils.randomAlphanumeric(10));
		personalInfo.setPhoneNumber(RandomStringUtils.randomAlphanumeric(10));
		personalInfo.setEmail("test@" + RandomStringUtils.randomAlphanumeric(4) + ".com");
		account.setPersonalInfo(personalInfo);
		account.setAccountInfo(accountInfo);
		if(balance==null) {
			balance = Math.abs((RANGE) * (double)r.nextInt(1000));
		}
		account.setBalance(balance);
		return account;
	}
	
	protected void deleteTestData() {
		Database db = new Database();
		db.connect();
		// delete accounts and logs that their accountNo starts with test_stress
		db.deleteAllAccounts();
		db.deleteAllLogs();
		db.close();
	}
	

	protected String createNewAccount(Double balance) {
		try {
			Account account = createRandomAccount(balance);
			final Response response = RestAssured.given().contentType(MediaType.APPLICATION_JSON_VALUE).body(account)
					.post(API_ROOT + "accounts");
			Assert.isTrue(response.getStatusCode() == 201);
			return response.jsonPath().get("transaction_id").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected Account getTransactionResult(String transactionId) {
		final ApiResponseWithData<Account> response = RestAssured.get(API_ROOT + "transactions/" + transactionId).then().statusCode(HttpStatus.OK.value())
				.extract().as(ApiResponseWithData.class);
		return toAccount((Map<String, Object>) response.getData());
	}
	

	protected Account toAccount(Map<String, Object> map) {
		if(map==null) {
			return null;
		}
		Account a = new Account();
		a.setAccountID((Long) map.get("accountID"));
		a.setBalance((Double) map.get("balance"));
		Map<String, Object> accountInfoMap = (Map<String, Object>) map.get("accountInfo");
		Map<String, Object> personalInfoMap = (Map<String, Object>) map.get("personalInfo");
		if(accountInfoMap!=null) {
			AccountInfo info = new AccountInfo();
			info.setAccountNo((String) accountInfoMap.get("accountNo"));
			info.setCreatedAt((String) accountInfoMap.get("createdAt"));
			info.setCurrency((String) accountInfoMap.get("currency"));
			Integer type = (Integer) accountInfoMap.get("type");
			if(type!=null) {
				info.setType(type.shortValue());
			}
			a.setAccountInfo(info);
		}
		if(personalInfoMap!=null) {
			PersonalInfo info = new PersonalInfo();
			info.setFirstname((String) personalInfoMap.get("firstname"));
			info.setLastname((String) personalInfoMap.get("lastname"));
			info.setBirthdate((String) personalInfoMap.get("birthdate"));
			info.setPassportNo((String) personalInfoMap.get("passportNo"));
			info.setAddress((String) personalInfoMap.get("address"));
			info.setEmail((String) personalInfoMap.get("email"));
			info.setPhoneNumber((String) personalInfoMap.get("phoneNumber"));
			info.setMobileNumber((String) personalInfoMap.get("mobileNumber"));
			a.setPersonalInfo(info);
		}
		return a;
	}

}
