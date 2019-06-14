package com.mobilab.accountservice.bussiness.messaging;

import java.util.Map;

import com.mobilab.accountservice.bussiness.message.MyRequest;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * custom serializer for gate - serialize MyRequest objects
 */

public class RequestSerializer implements Serializer<MyRequest> {
	
	final static Logger logger = Logger.getLogger(RequestSerializer.class);

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, MyRequest data) {
		byte[] retVal = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			retVal = objectMapper.writeValueAsString(data).getBytes();
		} catch (Exception ex) {
			logger.error("serialize error : "+data);
			logger.error(ex);
		}
		return retVal;
	}

	@Override
	public void close() {
	}
}