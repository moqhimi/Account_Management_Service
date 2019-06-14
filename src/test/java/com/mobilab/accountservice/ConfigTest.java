package com.mobilab.accountservice;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.utils.Config;
import com.mobilab.accountservice.bussiness.utils.DataUtils;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.PersonalInfo;

import java.util.Map;

public class ConfigTest {

	@Test
	public void testDatautils() {
		Boolean b1 = null;
		Boolean b2 = null;
		String accountNo = AccountFactory.generateAccountNumber();
		assertEquals(DataUtils.areNullOrEqual(b1, b2), true);
		assertEquals(DataUtils.areNullOrEqual(Double.valueOf(2d), 2d), true);
		assertEquals(DataUtils.areNullOrEqual(Float.valueOf(10f), 10f), true);
		assertEquals(DataUtils.areNullOrEqual(Long.valueOf(3l), 3l), true);
		assertEquals(DataUtils.areNullOrEqual(Boolean.valueOf(false), false), true);
		assertEquals(DataUtils.areNullOrEqual(Integer.valueOf(6), 6), true);
		assertEquals(DataUtils.areNullOrEqual(Double.valueOf(2d), 5d), false);
		assertEquals(DataUtils.areNullOrEqual(Short.valueOf((short)9), (short)9), true);
		assertEquals(DataUtils.accountsAreEqual(AccountFactory.account(accountNo), 
				AccountFactory.account(accountNo)), true);
		assertEquals(DataUtils.personalInfoAreEqual(new PersonalInfo("firstname", "lastname", 
				"birthdate", "passportNo", "address", "email", "phoneNumber", "mobileNumber"), 
				new PersonalInfo("firstname", "lastname", 
						"birthdate", "passportNo", "address", "email", "phoneNumber", "mobileNumber")),
				true);
		assertEquals(DataUtils.accountInfoAreEqual(new AccountInfo(AccountInfo.TYPE_SAVINGS, 
				accountNo, "currency", "createdAt"), 
				new AccountInfo(AccountInfo.TYPE_SAVINGS, 
						accountNo, "currency", "createdAt")), true);
	}
	
	@Test
	public void testDefault() {
		assertEquals(Config.getStringProperty("notexist", "default"), "default");
		assertEquals(Config.getNotNull(Double.valueOf(15d)), 15d, 0.001d);
		assertEquals(Config.getNotNull(Short.valueOf((short)10)), (short)10);
		assertEquals(Config.getNotNull("string"), "string");
	}
	
	@Test
	public void testGetValues() {
		Map<Object, Object> props = Config.getConfig(null);
		for(Object key : props.keySet()) {
			String value = props.get(key).toString();
			assertEquals(Config.getStringProperty(key.toString(), null), value);
		}
	}
	
	@Test
	public void testTypeConversion() {
		assertEquals(Config.toBoolean("true", null), true);
		assertEquals(Config.toBoolean("false", null), false);
		assertEquals(Config.toDouble("1.1", null).doubleValue(), 1.1d, 0.001d);
		assertEquals(Config.toInt("2", null).intValue(), 2);
		assertEquals(Config.toFloat("2.2", null).floatValue(), 2.2f, 0.001f);
		assertEquals(Config.toShort("10", null).shortValue(), (short)10);
		assertEquals(Config.toLong("897876876876", null).longValue(), 897876876876l);
	}
	
}
