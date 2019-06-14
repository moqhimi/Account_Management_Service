package com.mobilab.accountservice.bussiness.messaging;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

/**
 * represents the partitioner class to show Kafka to select which partition to choose 
 * for each request / response.
 * for every partition there is only one consumer due to resolve race condition.
 */

public class MyPartitioner implements Partitioner {
	
	@Override
	public void configure(Map<String, ?> configs) {
	}

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, 
			byte[] valueBytes, Cluster cluster) {
        return this.partition(key, cluster.partitionsForTopic(topic).size());
	}
	
	/**
	 * select partition number
	 * @param key - is computed from AccountKey hashCode
	 * @param numPartitions - number of partitions for topic
	 * @return the selected partition number
	 */
	public int partition(Object key, int numPartitions) {
        return Integer.parseInt(key.toString()) % numPartitions;
	}

	@Override
	public void close() {
	}
}