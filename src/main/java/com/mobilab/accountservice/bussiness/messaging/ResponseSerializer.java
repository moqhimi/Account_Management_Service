package com.mobilab.accountservice.bussiness.messaging;

import java.util.Map;

import com.mobilab.accountservice.bussiness.message.MyResponse;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * custom serializer for business handler - serialize MyResponse objects
 */

public class ResponseSerializer implements Serializer<MyResponse> {
	
	final static Logger logger = Logger.getLogger(ResponseSerializer.class);

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public byte[] serialize(String topic, MyResponse data) {
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