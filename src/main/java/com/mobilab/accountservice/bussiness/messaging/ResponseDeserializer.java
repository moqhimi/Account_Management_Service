package com.mobilab.accountservice.bussiness.messaging;

import java.util.Map;

import com.mobilab.accountservice.bussiness.message.MyResponse;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * custom deserializer for gate- deserialize MyRequest objects
 */

public class ResponseDeserializer implements Deserializer<MyResponse> {
	
	final static Logger logger = Logger.getLogger(ResponseDeserializer.class);

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public MyResponse deserialize(String topic, byte[] data) {
		ObjectMapper mapper = new ObjectMapper();
		MyResponse object = null;
		try {
			object = mapper.readValue(data, MyResponse.class);
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