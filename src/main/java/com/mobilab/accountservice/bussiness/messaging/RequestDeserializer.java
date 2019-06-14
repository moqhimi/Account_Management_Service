package com.mobilab.accountservice.bussiness.messaging;

import java.util.Map;

import com.mobilab.accountservice.bussiness.message.MyRequest;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * custom deserializer for business handler- deserialize MyRequest objects
 */

public class RequestDeserializer implements Deserializer<MyRequest> {
	
	final static Logger logger = Logger.getLogger(RequestDeserializer.class);

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public MyRequest deserialize(String topic, byte[] data) {
		ObjectMapper mapper = new ObjectMapper();
		MyRequest object = null;
		try {
			object = mapper.readValue(data, MyRequest.class);
		} catch (Exception ex) {
			logger.error("deserialize error : "+data);
			logger.error(ex);
		}
		return object;
	}

	@Override
	public void close() {
	}
}