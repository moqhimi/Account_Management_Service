package com.mobilab.accountservice.bussiness.process;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mobilab.accountservice.bussiness.messaging.KafkaFactory;
import com.mobilab.accountservice.bussiness.messaging.MyRebalanceListener;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.LogEntity;
import com.mobilab.accountservice.entities.LogEntityFactory;
import com.mobilab.accountservice.entities.UpdateLogEntity;
import com.mobilab.accountservice.entities.FinancialLogEntity;
import com.mobilab.accountservice.bussiness.CurrencyExchage.ExchangeWebService;
import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.CurrencyMap;
import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.message.ResponseFactory;
import com.mobilab.accountservice.bussiness.utils.Config;
import com.mobilab.accountservice.bussiness.utils.DataUtils;
import com.mobilab.accountservice.bussiness.utils.TopicUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

/**
 * a process to handle database and business logic. it is consumer of requests procued by gateway 
 * and return responses to gateway.
 */
public class BusinessHandler extends Handler {

	final static Logger logger = Logger.getLogger(BusinessHandler.class);

	private final Long businessHandlerID;
	
	/**
	 * consume messages from gateway
	 */
	private final KafkaConsumer<Long, MyRequest> consumerGate;
	
	/**
	 * produce messages for gateway
	 */
	private final KafkaProducer<Long, MyResponse> producerGate;

	private final MyRebalanceListener rebalanceListenerGate;
	
	/**
	 * is true until the process stops by shutdown hook listener
	 */
	private boolean running = true;
	
	/**
	 * maintain produced responses to send them in one transaction
	 */
	private final ConcurrentLinkedQueue<MyResponse> toPublishGate;

	/**
	 * to maintain db requests and process them in batch mode
	 */
	private final ConcurrentLinkedQueue<MyRequest> batchList;

	private Database db;

	public BusinessHandler(Long businessHandlerID) {
		this.businessHandlerID = businessHandlerID;
		this.toPublishGate = new ConcurrentLinkedQueue<MyResponse>();
		this.batchList = new ConcurrentLinkedQueue<MyRequest>();
		this.db = new Database(Config.getStringProperty("mongodb_host", "localhost:27017"),
				Config.getStringProperty("mongodb_username", null), Config.getStringProperty("mongodb_password", null));
		this.consumerGate = KafkaFactory.requestConsumer(this.businessHandlerID.toString(), "gate");
		this.producerGate = KafkaFactory.responseProducer(this.businessHandlerID.toString());
		this.rebalanceListenerGate = new MyRebalanceListener(this.consumerGate);
	}
	
	/**
	 * add message to list to publish it later in one transaction
	 * @param message - response to be delivered to the gateway
	 */
	private void publishToGate(MyResponse message) {
		logger.debug(String.format("BusinessHandler %d - put new message to publish to gate : %s",
				this.businessHandlerID, message));
		synchronized (toPublishGate) {
			toPublishGate.add(message);
		}
	}

	public ConcurrentLinkedQueue<MyResponse> getToPublishGate() {
		return toPublishGate;
	}

	public Database getDb() {
		return db;
	}
	
	/**
	 * starts connection to database and running threads
	 */
	@Override
	public void start() {
		logger.info("Start business process : " + this.businessHandlerID);

		this.db.connect();

		this.consumerGate.subscribe(Collections.singletonList(TopicUtils.TOPIC_DB_REQUEST), this.rebalanceListenerGate);
		this.producerGate.initTransactions();

		// this thread is because of sending multiple messages at the same time as one transaction. 
		// it collects messages for every one second and send all of them.
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					MyResponse message = null;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					synchronized (toPublishGate) {
						if (toPublishGate.size() > 0) {
							try {
								producerGate.beginTransaction();
								while ((message = toPublishGate.poll()) != null) {
									final Long key = message.partitioningKey();
									final ProducerRecord<Long, MyResponse> record = new ProducerRecord<Long, MyResponse>(
											TopicUtils.TOPIC_DB_RESPONSE, key, message);
									producerGate.send(record).get();
									logger.debug(String.format(
											"Business Handler %d - publish record key = %s", 
											businessHandlerID, message.getId()));
								}
								producerGate.commitTransaction();
							} catch (Exception e) {
								// if any exception occurs, the transaction should be aborted
								producerGate.abortTransaction();
								logger.error(
										"Business Handler " + businessHandlerID + "Producer Error in sending record");
								logger.error(e);
							}
						}
					}
				}
			}
		}).start();

		// this thread is responsible for wait for incoming requests and execute them in batch manner
		new Thread(new Runnable() {
			@Override
			public void run() {
				// there is three kinds of batch operations and each of them have their own 
				// list to process
				final List<MyRequest> listCreate = new ArrayList<MyRequest>();
				final List<MyRequest> listUpdate = new ArrayList<MyRequest>();
				final List<MyRequest> listRead = new ArrayList<MyRequest>();
				short type;
				MyRequest message;
				while (running) {
					listCreate.clear();
					listRead.clear();
					listUpdate.clear();
					synchronized (batchList) {
						try {
							while ((message = batchList.poll()) != null) {
								// decide to which list the message should be added according to 
								// the type of the request
								type = message.getRequestType();
								if (type == MyRequest.REQUEST_TYPE_CREATE_ACCOUNT) {
									listCreate.add(message);
								} else if (type == MyRequest.REQUEST_TYPE_READ_ACCOUNT) {
									listRead.add(message);
								} else if (type == MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT
										|| type == MyRequest.REQUEST_TYPE_DEPOSIT
										|| type == MyRequest.REQUEST_TYPE_TRANSFER
										|| type == MyRequest.REQUEST_TYPE_WITHDRAWAL) {
									listUpdate.add(message);
								}
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
					// execute batch operations
					batchInsert(listCreate);
					batchUpdate(listUpdate);
					batchRead(listRead);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();

		// blocking consumer. it is running until the shutdown hook trigger close
		try {
			while (this.running) {
				// block until one second and collect all records received during this time.
				// if no message has arrived, count will be 0.
				final ConsumerRecords<Long, MyRequest> records = 
						this.consumerGate.poll(Duration.ofSeconds(1));
				int count = records.count();
				if (count > 0) {
					logger.debug(String.format("Business Handler %d - receive messages = %d ", businessHandlerID,
							records.count()));
				}
				for (TopicPartition partition : records.partitions()) {
					List<ConsumerRecord<Long, MyRequest>> partitionRecords = records.records(partition);
					for (ConsumerRecord<Long, MyRequest> record : partitionRecords) {
						logger.debug(String.format(
								"Business Handler %d -partition = %d, key = %d, " + "offset = %d, value = %s",
								businessHandlerID, record.partition(), record.key(), record.offset(), record.value()));
						// set the offset to avoid re-processing the same request after failure
						rebalanceListenerGate.addOffset(record.topic(), record.partition(), record.offset());
						synchronized (this.batchList) {
							// add request to batchList to processed in batch thread
							this.batchList.add(record.value());
						}
					}
					// commit consumed messages. this ensures that no other process will process 
					// this message later, even if this process fail.
					long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
					consumerGate.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
				}
			}
		} finally {
			// when shutdown hook trigger close or any exception takes place
			this.consumerGate.close();
		}

	}
	
	/**
	 * perform batch insert operation
	 * @param list - list of MyRequest objects
	 */
	private void batchInsert(List<MyRequest> list) {
		if (list.size() == 0) {
			return;
		}
		// maintain responses for each account 
		final Map<Account, MyResponse> responseOfAccountMap = new HashMap<Account, MyResponse>();
		// maintain logs to insert in batch mode 
		final Map<Long, LogEntity> logs = new HashMap<Long, LogEntity>();
		// initialize lists to insert
		list.forEach(new Consumer<MyRequest>() {
			@Override
			public void accept(MyRequest message) {
				responseOfAccountMap.put(message.getAccount(), ResponseFactory.create(message.getId(),
						message.getGateID(), message.getSender(), (short) 0, null, message.getAccount()));
				logs.put(message.getId(), LogEntityFactory.create(message, false, null));
			}
		});
		try {
			logger.debug(
					String.format("Number of accounts to be inserted in bulk mode = %d", responseOfAccountMap.size()));
			final List<Account> insertList = new ArrayList<Account>(responseOfAccountMap.keySet());
			// accounts after creation
			final List<Account> insertedList = db.batchInsert(insertList);
			responseOfAccountMap.forEach(new BiConsumer<Account, MyResponse>() {
				@Override
				public void accept(Account a1, MyResponse res) {
					// if id is null it shows that it did not be inserted ,
					// might because of duplicate or so
					Account a = null;
					try {
						a = insertedList.get(insertedList.indexOf(a1));
					} catch (Exception ex) {}
					// set account after for log to inserted account
					((UpdateLogEntity) logs.get(res.getId())).setAccountAfter(a);
					// set status to OK if inserted account is not null
					// set status of log
					if (a != null) {
						logs.get(res.getId()).setStatus(true);
						res.setStatus(MyResponse.STATUS_OK);
						res.setAccount(a);
					} else {
						logs.get(res.getId()).setStatus(false);
						res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
					}
					logger.debug(String.format("Batch insert result = %s", res));
					// publish results to gateway
					publishToGate(res);
				}
			});
			// insert logs
			this.db.insertLog(logs.values());
		} catch (Exception e) {
			logger.error(e);
			// send error message for each request
			// this exception might be because of database failure for all 
			// of the requests or connection error
			for (MyResponse res : responseOfAccountMap.values()) {
				logs.get(res.getId()).setStatus(false);
				res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
				res.setError(e.getMessage());
				logger.debug(String.format("Batch insert result = %s", res));
				publishToGate(res);
			}
			// insert logs for failed transactions
			this.db.insertLog(logs.values());
		}
	}

	/**
	 * perform batch update operation
	 * @param list - list of MyRequest objects
	 */
	public void batchUpdate(List<MyRequest> list) {
		final Map<Account, MyResponse> responseOfAccountMap = new HashMap<Account, MyResponse>();
		final List<Account> accountList = new ArrayList<Account>();
		final Map<Long, LogEntity> logs = new HashMap<Long, LogEntity>();
		// initialize lists to be updated
		for (MyRequest r : list) {
			accountList.add(r.getAccount());
			// determine the type of log object based on the type of each request
			switch (r.getRequestType()) {
			case MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT:
				logs.put(r.getId(), LogEntityFactory.update(r, false, null, null));
				break;

			case MyRequest.REQUEST_TYPE_DEPOSIT:
				logs.put(r.getId(), LogEntityFactory.deposit(r, false, null, null));
				break;

			case MyRequest.REQUEST_TYPE_WITHDRAWAL:
				logs.put(r.getId(), LogEntityFactory.withdrawal(r, false, null, null));
				break;

			case MyRequest.REQUEST_TYPE_TRANSFER:
				logs.put(r.getId(), LogEntityFactory.transfer(r, false, null, null, r.getFinancialAccountNo2()));
				break;

			default:
				break;
			}
		}
		// read state of objects before start operations
		final List<Account> beforeList = this.db.batchRead(accountList);
		// maintail requests and for transfer requests, currency conversion information
		final Map<MyRequest, CurrencyMap> requestsList = new HashMap<MyRequest, CurrencyMap>();
		
		list.forEach(new Consumer<MyRequest>() {
			@Override
			public void accept(MyRequest message) {
				Account before = null;
				try {
					before = beforeList.get(beforeList.indexOf(message.getAccount()));
				} catch (Exception ex) {}
				// for transfer requests, one extra check is required because it should be 
				// confirmed that the destination account exists
				if (message.getRequestType() != MyRequest.REQUEST_TYPE_TRANSFER) {
					requestsList.put(message, null);
				}
				// for financial requests, set the current balance of request to the balance of before
				// also set the field balanceBefore for log
				if (message.getRequestType() == MyRequest.REQUEST_TYPE_TRANSFER
						|| message.getRequestType() == MyRequest.REQUEST_TYPE_WITHDRAWAL
						|| message.getRequestType() == MyRequest.REQUEST_TYPE_DEPOSIT) {
					double balanceBefore = before == null ? 0d : before.getBalance().doubleValue();
					message.getAccount().setBalance(balanceBefore);
					((FinancialLogEntity) logs.get(message.getId())).setBalanceBefore(balanceBefore);
				}
				
				// initialize response objects and fields of before for log
				switch (message.getRequestType()) {

				case MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT:
					responseOfAccountMap.put(message.getAccount(), ResponseFactory.update(message.getId(),
							message.getGateID(), message.getSender(), (short) 0, null, null));
					((UpdateLogEntity) logs.get(message.getId())).setAccountBefore(before);
					break;

				case MyRequest.REQUEST_TYPE_DEPOSIT:
					responseOfAccountMap.put(message.getAccount(),
							ResponseFactory.deposit(message.getId(), message.getGateID(), message.getSender(),
									(short) 0, null, message.getAccount().getAccountInfo().getAccountNo(), null));
					break;

				case MyRequest.REQUEST_TYPE_WITHDRAWAL:
					responseOfAccountMap.put(message.getAccount(),
							ResponseFactory.withdrawal(message.getId(), message.getGateID(), message.getSender(),
									(short) 0, null, message.getAccount().getAccountInfo().getAccountNo(), null));
					break;

				case MyRequest.REQUEST_TYPE_TRANSFER:
					// check if account2 exists
					try {
						Account a2 = db.readAccount(AccountFactory.account(message.getFinancialAccountNo2()));
						responseOfAccountMap.put(message.getAccount(),
								ResponseFactory.transfer(message.getId(), message.getGateID(), message.getSender(),
										MyResponse.STATUS_OK, null,
										message.getAccount().getAccountInfo().getAccountNo(), null));
						String fromcurrency = AccountFactory.currency(before);
						String tocurrency = AccountFactory.currency(a2);
						// call 3rd party API to get the currency exchange rate if currencyFrom 
						// and currencyTo are not equal
						requestsList.put(message, new CurrencyMap(fromcurrency, tocurrency,
								ExchangeWebService.getWebRate(fromcurrency, tocurrency)));
					} catch (Exception e) {
						// return status that destination account does not exist
						responseOfAccountMap.put(message.getAccount(),
								ResponseFactory.transfer(message.getId(), message.getGateID(), message.getSender(),
										MyResponse.STATUS_ACCOUNT_DOESNT_EXIST, null,
										message.getAccount().getAccountInfo().getAccountNo(), null));
						logs.get(message.getId()).setStatus(false);
						requestsList.put(message, null);
					}
					break;

				default:
					break;
				}
			}
		});
		if (responseOfAccountMap.size() == 0) {
			return;
		}
		try {
			logger.debug(
					String.format("Number of accounts to be updated in bulk mode = %d", responseOfAccountMap.size()));
			// perform batch update operations and return the updated state
			final List<Account> result = db.batchUpdate(requestsList);
			responseOfAccountMap.forEach(new BiConsumer<Account, MyResponse>() {
				@Override
				public void accept(Account a, MyResponse res) {
					// check if updated state exists
					Account updated = null;
					try {
						updated = result.get(result.indexOf(a));
					} catch (Exception ex) {}
					finally {
						if (updated == null) {
							// account does not exist in update results
							res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
							logs.get(res.getId()).setStatus(false);
						} else {
							System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+updated.getAccountInfo().getAccountNo()+" , "+updated.getBalance());
							// if id is null it shows that it did not be read,
							// might because it does not exist
							if (updated.getAccountID() == null) {
								res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
								logs.get(res.getId()).setStatus(false);
							} else {
								res.setAccount(updated);
								if (res.getStatus() == 0) {
									res.setStatus(MyResponse.STATUS_OK);
								}
								// check if no change has occurred return failure
								Account before = null;
								try {
									before = beforeList.get(beforeList.indexOf(a));
								} catch (Exception ex) {}
								// account does not exist before update
								if (before == null) {
									res.setStatus(MyResponse.STATUS_ACCOUNT_DOESNT_EXIST);
									logs.get(res.getId()).setStatus(false);
								} 
								else {
									double balanceBefore = before.getBalance() == null ? 0d
											: before.getBalance().doubleValue();

									boolean change = false;
									// check whether fields have changed depending 
									// on the type of the request. Also set after fields of log.
									switch (res.getResponseType()) {

									case MyResponse.RESPONSE_TYPE_UPDATE_ACCOUNT:
										change = (balanceBefore != res.getAccount().getBalance().doubleValue())
												|| !DataUtils.accountsAreEqual(a, res.getAccount());
										((UpdateLogEntity) logs.get(res.getId())).setAccountAfter(updated);
										break;

									case MyResponse.RESPONSE_TYPE_DEPOSIT:
										change = balanceBefore != res.getAccount().getBalance().doubleValue();
										((FinancialLogEntity) logs.get(res.getId()))
												.setBalanceAfter(updated == null ? 0d : updated.getBalance());
										break;

									case MyResponse.RESPONSE_TYPE_WITHDRAWAL:
										change = balanceBefore != res.getAccount().getBalance().doubleValue();
										((FinancialLogEntity) logs.get(res.getId()))
												.setBalanceAfter(updated == null ? 0d : updated.getBalance());
										break;

									case MyResponse.RESPONSE_TYPE_TRANSFER:
										change = balanceBefore != res.getAccount().getBalance().doubleValue();
										((FinancialLogEntity) logs.get(res.getId()))
												.setBalanceAfter(updated == null ? 0d : updated.getBalance());
										break;

									default:
										return;
									}
									// set status of log according to the change value
									if (change) {
										logs.get(res.getId()).setStatus(true);
									}
									else {
										logs.get(res.getId()).setStatus(false);
										if (res.getResponseType() == MyResponse.RESPONSE_TYPE_WITHDRAWAL
												|| res.getResponseType() == MyResponse.RESPONSE_TYPE_TRANSFER) {
											res.setStatus(MyResponse.STATUS_AMOUNT_EXCEEDS);
										} else {
											res.setStatus(MyResponse.STATUS_FAILURE);
										}
									}
								}
							}
						}
					}
					logger.debug(String.format("Batch update result = %s", res));
					// publish update results to the gateway
					publishToGate(res);
				}
			});
			// insert logs
			db.insertLog(logs.values());
		} catch (Exception e) {
			e.printStackTrace();
			// update error in database
			logger.error(e);
			responseOfAccountMap.forEach(new BiConsumer<Account, MyResponse>() {
				@Override
				public void accept(Account a, MyResponse res) {
					// if connection failure or any general condition takes place, 
					// reject all of the requests
					logs.get(res.getId()).setStatus(false);
					res.setAccount(a);
					res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
					res.setError(e.getMessage());
					logger.debug(String.format("Batch update result = %s", res));
					publishToGate(res);
				}
			});
			this.db.insertLog(logs.values());
		}
	}

	/**
	 * perform batch read operation
	 * @param list - list of MyRequest objects
	 */
	private void batchRead(List<MyRequest> list) {
		final Map<Account, MyResponse> responseOfAccountMap = new HashMap<Account, MyResponse>();
		// initialize list to read
		list.forEach(new Consumer<MyRequest>() {
			@Override
			public void accept(MyRequest message) {
				responseOfAccountMap.put(message.getAccount(), ResponseFactory.read(message.getId(),
						message.getGateID(), message.getSender(), (short) 0, null, null));
			}
		});
		if (responseOfAccountMap.size() == 0) {
			return;
		}
		try {
			logger.debug(String.format(" in bulk mode = %d", responseOfAccountMap.size()));
			// get results from db
			final List<Account> result = db.batchRead(new ArrayList<Account>(responseOfAccountMap.keySet()));
			responseOfAccountMap.forEach(new BiConsumer<Account, MyResponse>() {
				@Override
				public void accept(Account a, MyResponse res) {
					try {
						res.setAccount(result.get(result.indexOf(a)));
						// if id is null it shows that it did not be read,
						// might because it does not exist
						if (res.getAccount().getAccountID() != null) {
							res.setStatus(MyResponse.STATUS_OK);
						} else {
							res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
						}
					} catch (Exception ex) {
						// account does not exist in result
						res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
					}
					logger.debug(String.format("Batch insert result = %s", res));
					publishToGate(res);
				}
			});
		} catch (Exception e) {
			logger.error(e);
			responseOfAccountMap.forEach(new BiConsumer<Account, MyResponse>() {
				@Override
				public void accept(Account a, MyResponse res) {
					// any kind of error irrespective of one specific request
					res.setAccount(a);
					res.setStatus(MyResponse.STATUS_DATABASE_ERROR);
					res.setError(e.getMessage());
					logger.debug(String.format("Batch insert result = %s", res));
					publishToGate(res);
				}
			});
		}
	}
	
	/**
	 * called when the process is interrupted and shutdown hook is called.
	 * it sets running to false (to terminate threads) and closes database connection.
	 */
	@Override
	public void stop() {
		logger.info("Stop business process : " + this.businessHandlerID);
		this.running = false;
		this.db.close();
	}

}
