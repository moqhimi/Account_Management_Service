package com.mobilab.accountservice;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.AccountKey;
import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.message.RequestFactory;
import com.mobilab.accountservice.bussiness.message.ResponseFactory;
import com.mobilab.accountservice.bussiness.messaging.MyPartitioner;
import com.mobilab.accountservice.bussiness.messaging.RequestDeserializer;
import com.mobilab.accountservice.bussiness.messaging.RequestSerializer;
import com.mobilab.accountservice.bussiness.messaging.ResponseDeserializer;
import com.mobilab.accountservice.bussiness.messaging.ResponseSerializer;
import com.mobilab.accountservice.bussiness.utils.TopicUtils;

public class MessagingTest {
	
	@Test
	public void testPartitioner() {
		MyPartitioner partitioner = new MyPartitioner();
		assertEquals(partitioner.partition("1",3), 1);
		assertEquals(partitioner.partition("2",3), 2);
		assertEquals(partitioner.partition("3",3), 0);
		assertEquals(partitioner.partition("4",3), 1);
		String accountNo = AccountFactory.generateAccountNumber();
		MyRequest request = RequestFactory.withdrawal(1l, 2l, null, accountNo, 3d);
		assertEquals(request.partitioningKey().longValue(),
				Long.valueOf(new AccountKey(accountNo).hashCode()).longValue());
		MyResponse response = ResponseFactory.withdrawal(1l, 2l, null, (short)1, null, accountNo, 100d);
		assertEquals(response.partitioningKey().longValue(),
				Long.valueOf(new AccountKey(accountNo).hashCode()).longValue());
		partitioner.close();
	}
	
	@Test
	public void testRequestSerializer() {
		String accountNo = AccountFactory.generateAccountNumber();
		MyRequest request = RequestFactory.withdrawal(1l, 2l, null, accountNo, 3d);
		RequestSerializer serializer = new RequestSerializer();
		RequestDeserializer deserializer = new RequestDeserializer();
		byte[] data = serializer.serialize(TopicUtils.TOPIC_DB_REQUEST, request);
		MyRequest request2 = deserializer.deserialize(TopicUtils.TOPIC_DB_REQUEST, data);
		assertEquals(request, request2);
		assertEquals(request.getId(), request2.getId());
		assertEquals(request.getGateID(), request2.getGateID());
		assertEquals(request.getFinancialAmount().doubleValue(), 
				request2.getFinancialAmount().doubleValue(), 0.001d);
		assertEquals(request.getAccount().getAccountInfo().getAccountNo(), 
				request2.getAccount().getAccountInfo().getAccountNo());
	}
	
	@Test
	public void testResponseSerializer() {
		String accountNo = AccountFactory.generateAccountNumber();
		MyResponse response = ResponseFactory.withdrawal(1l, 2l, null, (short)0, null, 
				accountNo, 100000d);
		ResponseSerializer serializer = new ResponseSerializer();
		ResponseDeserializer deserializer = new ResponseDeserializer();
		byte[] data = serializer.serialize(TopicUtils.TOPIC_DB_RESPONSE, response);
		MyResponse response2 = deserializer.deserialize(TopicUtils.TOPIC_DB_RESPONSE, data);
		assertEquals(response, response2);
		assertEquals(response.getId(), response2.getId());
		assertEquals(response.getGateID(), response2.getGateID());
		assertEquals(response.getAccount().getBalance().doubleValue(), 
				response2.getAccount().getBalance().doubleValue(), 0.001d);
		assertEquals(response.getAccount().getAccountInfo().getAccountNo(), 
				response2.getAccount().getAccountInfo().getAccountNo());
	}
	
}
