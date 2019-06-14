package com.mobilab.accountservice.bussiness.process;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.message.RequestFactory;
import com.mobilab.accountservice.bussiness.messaging.KafkaFactory;
import com.mobilab.accountservice.bussiness.messaging.MyRebalanceListener;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.service.TransactionResultService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import com.mobilab.accountservice.bussiness.utils.TopicUtils;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A process which is responsible to get requests from restful API and send them 
 * to topics to be processed by business handlers
 */

public class Gateway extends Handler {
	@Autowired
	TransactionResultService transactionService;

	final static Logger logger = Logger.getLogger(Gateway.class);
	
	/*
	 * a random unique ID for gateway process
	 */
	private Long gateID;

	/*
	 * consumer to receive responses from business handler
	 */
	private KafkaConsumer<Long, MyResponse> consumer;

	/*
	 *  producer of requests to business handler
	 */
	private KafkaProducer<Long, MyRequest> producer;

	/*
	 * is invoked when a new node gets started or stopped
	 */
	private MyRebalanceListener rebalanceListener;
	
	/*
	 * is true until stop method is called by shutdown hook handler
	 */
	private boolean running = true;

	/*
	 *  submit new requests to this queue to be published via one transaction
	 */
	private final ConcurrentLinkedQueue<MyRequest> toPublish;

	/*
	 *  whether is a request gateway or not.
	 *  a request gateway could produce messages but not receive responses.
	 */
	private final boolean requestGate;

	/*
	 *  whether is a response gateway or not.
	 *  a response gateway could only receive responses not producing requests
	 */
	private final boolean responseGate;

	public Long getGateID() {
		return gateID;
	}

	public Gateway(Long gateID, boolean requestGate, boolean responseGate) {
		this.gateID = gateID;
		this.requestGate = requestGate;
		this.responseGate = responseGate;
		logger.info("Init gateway process : " + this.gateID + " , requestGate = " + this.requestGate
				+ " , responseGate = " + this.responseGate);
		this.toPublish = new ConcurrentLinkedQueue<MyRequest>();
		if (this.requestGate) {
			this.producer = KafkaFactory.requestProducer(this.gateID.toString());
		}
		if (this.responseGate) {
			this.consumer = KafkaFactory.responseConsumer(this.gateID.toString(), "gate");
			this.rebalanceListener = new MyRebalanceListener(consumer);
		}
	}
	
	/**
	 * is called when REST wants to create a new account
	 */
	public void createAccount(long messageID, String sender, Account account) {
		this.newRequest(RequestFactory.create(messageID, gateID, sender, account));
	}
	
	/**
	 * is called to handle a read account request
	 */
	public void readAccount(long messageID, String sender, String accountNo) {
		this.newRequest(RequestFactory.read(messageID, gateID, sender, accountNo));
	}

	/**
	 * publish a new request to the business handler
	 */
	public void newRequest(MyRequest message) {
		this.publishToBusiness(message);
	}
	
	/**
	 * is called when a new response from business handler arrives
	 */
	private void consumeBusinessMessage(MyResponse message) {
		transactionService.put(message);
	}
	
	/**
	 * add new request to the list to get published at once
	 */
	private void publishToBusiness(MyRequest message) {
		logger.debug(String.format("Gateway %d - put new message to publish : %s", this.gateID, message));
		synchronized (toPublish) {
			toPublish.add(message);
		}
	}

	/**
	 * start threads
	 */
	public void start() {
		logger.info("Start gateway process : " + this.gateID);
		if (this.requestGate) {
			// use transactions for producing requests
			this.producer.initTransactions();
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (running) {
						MyRequest message = null;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						synchronized (toPublish) {
							if (toPublish.size() > 0) {
								logger.debug(String.format("Gateway %d - Publish Queue Size = %d ", gateID, toPublish.size()));
								try {
									// send all of the requests received during sleep time in a single 
									// transaction
									producer.beginTransaction();
									while ((message = toPublish.poll()) != null) {
										final Long key = message.partitioningKey();
										final ProducerRecord<Long, MyRequest> record = new ProducerRecord<Long, MyRequest>(
												TopicUtils.TOPIC_DB_REQUEST, key, message);
										final RecordMetadata metadata = producer.send(record).get();
										logger.debug(
												String.format("Gateway %d - publish record key = %s , partition = %d, offset = %d ",
														gateID, key, metadata.partition(), metadata.offset()));
									}
									// commit transaction if all the messages are sent
									producer.commitTransaction();
								} catch (Exception e) {
									// abort transaction if any exception takes place
									producer.abortTransaction();
									logger.error("Gateway " + gateID + "Producer Error in sending record");
									logger.error(e);
								}
							}
						}
					}
					// called after stop gets called by shutdown hook
					producer.close();
				}
			}).start();
		}
		if (this.responseGate) {
			// subscribe to response topic
			this.consumer.subscribe(Collections.singletonList(TopicUtils.TOPIC_DB_RESPONSE), this.rebalanceListener);
			try {
				while (this.running) {
					// block and collect all the responses arrived during one second.
					// in case of no message, count will be 0.
					final ConsumerRecords<Long, MyResponse> records = this.consumer.poll(Duration.ofSeconds(1));
					int count = records.count();
					if (count > 0) {
						logger.debug(String.format("Gateway %d - receive messages = %d ", gateID, count));
					}
					for (TopicPartition partition : records.partitions()) {
						List<ConsumerRecord<Long, MyResponse>> partitionRecords = records.records(partition);
						for (ConsumerRecord<Long, MyResponse> record : partitionRecords) {
							//logger.debug(String.format("Gateway %d -partition = %d, key = %d, " + "offset = %d, value = %s", gateID,
									//record.partition(), record.key(), record.offset(), record.value()));
							// set the latest offset in rebalancer
							rebalanceListener.addOffset(record.topic(), record.partition(), record.offset());
							// consume new message from business handler
							consumeBusinessMessage(record.value());
						}
						long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
						// commit consumed messages to avoid re-processing them again
						consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
					}
					// get a little sleep after each poll
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			} finally {
				// called after stop gets called by shutdown hook
				this.consumer.close();
			}
		}
	}

	/**
	 * called when the process is interrupted and shutdown hook is called.
	 * it sets running to false (to terminate threads) and closes database connection.
	 */
	public void stop() {
		logger.info("Stop running gateway process : " + this.gateID);
		this.running = false;
	}

}