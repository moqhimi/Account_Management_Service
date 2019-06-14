package com.mobilab.accountservice.bussiness.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

/**
 * when a node is added, removed or closed, rebalancing partition assignments take place.
 * this class ensure not processing any duplicated offset. 
 * this is a part of the exact-once policy in design.
 */

public class MyRebalanceListener implements ConsumerRebalanceListener {

	final static Logger logger = Logger.getLogger(MyRebalanceListener.class);

	private Consumer consumer;
	private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap();

	public MyRebalanceListener(Consumer con) {
		this.consumer = con;
	}

	public void addOffset(String topic, int partition, long offset) {
		currentOffsets.put(new TopicPartition(topic, partition), new OffsetAndMetadata(offset+1, "Commit"));
	}

	public Map<TopicPartition, OffsetAndMetadata> getCurrentOffsets() {
		return currentOffsets;
	}

	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		for (TopicPartition partition : partitions) {
			logger.debug(String.format("onPartitionsAssigned , partition = %s , position = %s",
					partition.partition(), consumer.position(partition)));
		}
	}

    /**
     * commit offset with whom it ensures that it couldn't be possible to re-process committed records
     */
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        for(TopicPartition partition: partitions) {
        	logger.debug(String.format("onPartitionsRevoked , partition = %s , position = %s",
					partition.partition(), consumer.position(partition)));
        }
        for(TopicPartition tp: currentOffsets.keySet()) {
        	logger.debug(String.format("onPartitionsRevoked , partition = %s , position = %s",
        			tp.partition(), consumer.position(tp)));
        }
        consumer.commitSync(currentOffsets);
        currentOffsets.clear();
    }

}
