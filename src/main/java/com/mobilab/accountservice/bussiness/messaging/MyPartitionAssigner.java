package com.mobilab.accountservice.bussiness.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.apache.kafka.common.TopicPartition;

/**
 * represents the way which each partition should be assigned to a consumer.
 * at time, this is the default implementation prepared by Kafka.
 */

public class MyPartitionAssigner extends AbstractPartitionAssignor{

	@Override
	public String name() {
		return "custom";
	}

	
    private Map<String, List<String>> consumersPerTopic(Map<String, Subscription> consumerMetadata) {
        Map<String, List<String>> res = new HashMap<>();
        for (Map.Entry<String, Subscription> subscriptionEntry : consumerMetadata.entrySet()) {
            String consumerId = subscriptionEntry.getKey();
            for (String topic : subscriptionEntry.getValue().topics()) {
                put(res, topic, consumerId);
            }
        }
        return res;
    }

    @Override
    public Map<String, List<TopicPartition>> assign(Map<String, Integer> partitionsPerTopic,
                                                    Map<String, Subscription> subscriptions) {
        Map<String, List<String>> consumersPerTopic = consumersPerTopic(subscriptions);
        Map<String, List<TopicPartition>> assignment = new HashMap<>();
        for (String memberId : subscriptions.keySet())
            assignment.put(memberId, new ArrayList<>());

        for (Map.Entry<String, List<String>> topicEntry : consumersPerTopic.entrySet()) {
            String topic = topicEntry.getKey();
            List<String> consumersForTopic = topicEntry.getValue();

            Integer numPartitionsForTopic = partitionsPerTopic.get(topic);
            if (numPartitionsForTopic == null)
                continue;

            Collections.sort(consumersForTopic);

            int numPartitionsPerConsumer = numPartitionsForTopic / consumersForTopic.size();
            int consumersWithExtraPartition = numPartitionsForTopic % consumersForTopic.size();

            List<TopicPartition> partitions = AbstractPartitionAssignor.partitions(topic, numPartitionsForTopic);
            for (int i = 0, n = consumersForTopic.size(); i < n; i++) {
                int start = numPartitionsPerConsumer * i + Math.min(i, consumersWithExtraPartition);
                int length = numPartitionsPerConsumer + (i + 1 > consumersWithExtraPartition ? 0 : 1);
                assignment.get(consumersForTopic.get(i)).addAll(partitions.subList(start, start + length));
            }
        }
        /*
        for(String consumer : assignment.keySet()) {
        	Long id = null;
        	try {
        		id = Long.parseLong(consumer.substring(0,consumer.indexOf('-')));
        	}
        	catch(Exception ex) {}
        	if(id==null) {
        		continue;
        	}
        	List<TopicPartition> assigned = assignment.get(consumer);
        	for(String topic : partitionsPerTopic.keySet()) {
                Integer numPartitionsForTopic = partitionsPerTopic.get(topic);
                if (numPartitionsForTopic == null)
                    continue;
                int remainder = (int)(id % numPartitionsForTopic.intValue());
                List<TopicPartition> partitions = AbstractPartitionAssignor.partitions(topic, 
                		numPartitionsForTopic);
                for(TopicPartition tp : partitions) {
                	if(assigned.contains(tp)) {
                		continue;
                	}
                	if(tp.partition()==remainder) {
                	}
                }
        	}
        }
        */
        return assignment;
    }

}
