package com.mobilab.accountservice.bussiness.messaging;

import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.log4j.Logger;

import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.utils.Config;

/**
 * a factory class to create consumers and producers which are required in gate and business handler
 */

public class KafkaFactory {

	final static Logger logger = Logger.getLogger(KafkaFactory.class);
	
	/**
	 * create a Kafka consumer of requests which is used in business handler from configuration file
	 * @param clientID - a unique ID for each node (business handler)
	 * @param group - all nodes of business handler have the same group
	 * @return KafkaConsumer
	 */
	public static KafkaConsumer<Long, MyRequest> requestConsumer(String clientID, String group) {
		Properties props = new Properties();
		/**
		 * read properties start with 'kafka.' and remove 'kafka.' and then append it to properties.
		 * this is due to enable users to add any kind of configurations available 
		 * for Kafka consumers and producers.
		 */
		Config.getConfig("kafka.").forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object key, Object value) {
				props.put(key.toString(), value);
			}
		});
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientID);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RequestDeserializer.class.getName());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		/**
		 * only read those messages which have been committed by producer
		 */
		props.put("isolation.level", "read_committed");
		props.put("partition.assignment.strategy", MyPartitionAssigner.class.getName());
		logger.info(String.format("cteate consumer , group = %s , props = %s", group, props));
		return new KafkaConsumer<Long, MyRequest>(props);
	}
	
	/**
	 * create a Kafka consumer of responses which is used in gateway from configuration file
	 * @param clientID - a unique ID for each node (gateway)
	 * @param group - all nodes of gateway have the same group
	 * @return KafkaConsumer
	 */
	public static KafkaConsumer<Long, MyResponse> responseConsumer(String clientID, String group) {
		Properties props = new Properties();
		Config.getConfig("kafka.").forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object key, Object value) {
				props.put(key.toString(), value);
			}
		});
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientID);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ResponseDeserializer.class.getName());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put("isolation.level", "read_committed");
        props.put("partition.assignment.strategy", MyPartitionAssigner.class.getName());
		logger.info(String.format("cteate consumer , group = %s , props = %s", group, props));
		return new KafkaConsumer<Long, MyResponse>(props);
	}
	
	/**
	 * create a Kafka producer of requests which is used in gateway from configuration file
	 * @param clientID - a unique ID for each node (gateway)
	 * @return KafkaProducer
	 */
	public static KafkaProducer<Long, MyRequest> requestProducer(String clientID){
		Properties props = new Properties();
		Config.getConfig("kafka.").forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object key, Object value) {
				props.put(key.toString(), value);
			}
		});
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RequestSerializer.class.getName());
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());
        /**
         * produce in a transactional manner
         */
        props.put("enable.idempotence", "true");
        props.put("transactional.id", clientID);
		logger.info(String.format("cteate producer , client = %s , props = %s", clientID, props));
		return new KafkaProducer<Long, MyRequest>(props);
	}
	
	/**
	 * create a Kafka producer of responses which is used in business handler from configuration file
	 * @param clientID - a unique ID for each node (business handler)
	 * @return KafkaProducer
	 */
	public static KafkaProducer<Long, MyResponse> responseProducer(String clientID){
		Properties props = new Properties();
		Config.getConfig("kafka.").forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object key, Object value) {
				props.put(key.toString(), value);
			}
		});
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ResponseSerializer.class.getName());
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());
        props.put("enable.idempotence", "true");
        props.put("transactional.id", clientID);
		logger.info(String.format("cteate producer , client = %s , props = %s", clientID, props));
		return new KafkaProducer<Long, MyResponse>(props);
	}
	
}
