package com.mobilab.accountservice.bussiness.message;

/**
 * to get the key of message that is used in Kafka partitioning
 */

public interface PartitioningKeyGetter {
	
	public Long partitioningKey();

}
