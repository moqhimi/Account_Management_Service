package com.mobilab.accountservice.bussiness.process;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.CurrencyMap;
import com.mobilab.accountservice.bussiness.message.MyRequest;
import com.mobilab.accountservice.bussiness.message.ResponseFactory;
import com.mobilab.accountservice.bussiness.utils.Config;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.AccountInfo;
import com.mobilab.accountservice.entities.FinancialLogEntity;
import com.mobilab.accountservice.entities.LogEntity;
import com.mobilab.accountservice.entities.PersonalInfo;
import com.mobilab.accountservice.entities.TransferLogEntity;
import com.mobilab.accountservice.entities.UpdateLogEntity;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

import ch.qos.logback.classic.LoggerContext;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


/**
 * MongoDB interface for connection and CRUD operations on accounts and logs
 */

public class Database {

	static {
		// disable mongodb debug and info logs
		((LoggerContext) LoggerFactory.getILoggerFactory()).
			getLogger("org.mongodb.driver").setLevel(ch.qos.logback.classic.Level.ERROR);
	}
	
	final static Logger logger = Logger.getLogger(Database.class);
	
	/**
	 * database connection string, contains IP, port, credentials (optional)
	 */
	private final String uri;
	private MongoClient mongoClient;
	private MongoDatabase db;
	/*
	 * collection for accounts
	 */
	private MongoCollection<Account> accountCollection;
	/**
	 * collection of logs
	 */
	private MongoCollection<Document> logCollection;
	/**
	 * general representation of accounts collection in bson Document format
	 */
	private MongoCollection<Document> collection;
	/**
	 * used in encode-decode documents to accounts objects
	 */
	private final EncoderContext encoderContext;
	private final CodecRegistry pojoCodecRegistry;
	private final Codec<Account> pojoCodec;
	private final JsonWriterSettings jsonWriterSettings;
	/**
	 * convert logs to json and vice-versa
	 */
	private final ObjectMapper jsonMapper;
	
	public Database() {
		// in default constructor, parameters are read from config
		this(Config.getStringProperty("mongodb_host", "localhost:27017"),
				Config.getStringProperty("mongodb_username", null), 
				Config.getStringProperty("mongodb_password", null));
	}
	
	@SuppressWarnings("deprecation")
	public Database(String host, String username, String password) {
		logger.info(String.format("initialize mongodb , host = %s, username = %s , password = %s", host,
				username, password));
		this.jsonMapper = new ObjectMapper();
		//this.uri = String.format("mongodb://%s:%s@%s/?authSource=admin", username, password, host);
		this.uri = String.format("mongodb://%s", host);
		logger.debug("mongodb connection string = "+this.uri);
		this.encoderContext = EncoderContext.builder().build();
		this.pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		this.pojoCodec = this.pojoCodecRegistry.get(Account.class);
		this.jsonWriterSettings = new JsonWriterSettings(true);
	}
	
	/**
	 * connect to the database and initialize collections
	 */
	public void connect() {
		this.mongoClient = MongoClients.create(this.uri);
		this.db = this.mongoClient.getDatabase("bank");
		this.db = this.db.withCodecRegistry(pojoCodecRegistry);
		this.accountCollection = this.db.getCollection("accounts", Account.class);
		this.collection = this.db.withWriteConcern(WriteConcern.MAJORITY.withWTimeout(1000, 
				TimeUnit.MILLISECONDS)).getCollection("accounts");
		this.logCollection = this.db.withWriteConcern(WriteConcern.MAJORITY.withWTimeout(1000, 
				TimeUnit.MILLISECONDS)).getCollection("logs");
	}
	
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public MongoDatabase getDb() {
		return db;
	}

	public MongoCollection<Document> getLogCollection() {
		return logCollection;
	}

	public MongoCollection<Document> getCollection() {
		return collection;
	}

	public ObjectMapper getJsonMapper() {
		return jsonMapper;
	}
	
	/**
	 * close connection
	 */
	public void close() {
		if(this.mongoClient!=null) {
			this.mongoClient.close();
		}
	}
	
	/**
	 * get all accounts
	 */
	public void getAllAccounts(Consumer<Account> consumer) {
		this.accountCollection.find().forEach(consumer);
	}
	
	/**
	 * delete all accounts
	 */
	public void deleteAllAccounts(){
		this.accountCollection.deleteMany(new BasicDBObject());
	}
	
	/**
	 * converts Account object to json String
	 */
	public String toJson(Account a) {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter, jsonWriterSettings);
		this.pojoCodec.encode(jsonWriter, a, encoderContext);
		return stringWriter.toString();
	}
	
	/**
	 * create a new account if not exists.
	 * because the index is on accountNo field, it could not be duplicated.
	 */
	public void createAccount(Account account) throws Exception{
		this.accountCollection.insertOne(account);	
	}
	
	/**
	 * read an account
	 * @param account - contains only the accountNo field
	 * @return Account - DB version of account object if exists
	 */
	public Account readAccount(Account account) throws Exception{
		BasicDBObject q = new BasicDBObject("accountInfo.accountNo", 
				account.getAccountInfo().getAccountNo());
		final List<Account> list = new ArrayList<Account>();
		this.accountCollection.find(q).forEach(new Consumer<Account>() {
			@Override
			public void accept(Account a) {
				list.add(a);
			}
		});
		return list.size() > 0 ? list.get(0) : null;
	}

	public MongoCollection<Account> getAccountCollection() {
		return accountCollection;
	}
	
	/**
	 * perform batch insert operation on accounts in one transaction.
	 * skip accoutns which are duplicated.
	 * @param list - accounts to be inserted
	 * @return DB version of inserted accounts
	 */
	public List<Account> batchInsert(List<Account> list) throws Exception{
		// remove duplicates
		// try to read accounts and remove existing ones
		final List<Account> existList = this.batchRead(list);
		final List<Account> notExistList = new ArrayList<Account>();
		for(Account a : list) {
			if(!existList.contains(a)) {
				notExistList.add(a);
			}
		}
		final InsertManyOptions options = new InsertManyOptions();
		options.ordered(true);
		options.bypassDocumentValidation(true);
		// convert Account objects to Document
		final List<Document> docs = new ArrayList<Document>();
		for(Account a : notExistList) {
			Document doc = new Document().append("balance", a.getBalance()).append("personalInfo", 
					a.getPersonalInfo()).append("accountInfo", a.getAccountInfo())
					.append("accountID", a.getAccountID());
			docs.add(doc);
		}
		try {
			this.collection.insertMany(docs, options);
		}
		catch(Exception ex) {}
		int i = 0;
		// set id for those which are inserted.
		// existing accounts have ID of null.
		for(Document doc : docs) {
			notExistList.get(i).setAccountID(doc.getLong("accountID"));
			++i;
		}
		// return inserted accounts
		return notExistList;
	}
	
	/**
	 * perform batch read operation in one DB query
	 * @param list of accounts to be read
	 * @return DB version list of accounts that exist in DB
	 */
	public List<Account> batchRead(List<Account> list) {
		if(list.size()==0) {
			return null;
		}
		// create OR query based on accountNo field
		final BasicDBList or = new BasicDBList();
		list.forEach(new Consumer<Account>() {
			@Override
			public void accept(Account a) {
				final String accountNo = a.getAccountInfo().getAccountNo();
				if(accountNo==null || accountNo.length()==0) {
					return;
				}
				or.add(new BasicDBObject("accountInfo.accountNo", accountNo));
			}
		});
		final BasicDBObject query = new BasicDBObject("$or", or);
		final List<Account> result = new ArrayList<Account>();
		this.accountCollection.find(query).forEach(new Consumer<Account>() {
			@Override
			public void accept(Account a) {
				result.add(a);
			}
		});
		return result;
	}
	
	/**
	 * perform batch update operation on accounts in a single transaction
	 * @param list of requests
	 * @return DB version list of updated state of accounts
	 */
	public List<Account> batchUpdate(Map<MyRequest, CurrencyMap> list) {
		if(list.size()==0) {
			return null;
		}
		// list of write operations to be executed in batch mode
		final List<WriteModel<Account>> writes = new ArrayList<WriteModel<Account>>();
		list.keySet().forEach(new Consumer<MyRequest>() {
			@Override
			public void accept(MyRequest request) {
				// skip requests that are financial and does not have amount field or are transfer 
				// and does not have accountNo of destination
				Double amount = request.getFinancialAmount();
				if(request.getRequestType()!=MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT) {
					if(amount==null) {
						return;
					}
					if(request.getRequestType()==MyRequest.REQUEST_TYPE_TRANSFER && 
							request.getFinancialAccountNo2()==null) {
						return;
					}
				}
				
				String accountNo = request.getAccount().getAccountInfo().getAccountNo();

				if(accountNo==null || accountNo.length()==0) {
					return;
				}
				// and for query part of each update.
				// each write has 2 parts : a query and an update
				final List<BasicDBObject> andList = new ArrayList<BasicDBObject>();
				andList.add(new BasicDBObject().append("accountInfo.accountNo", accountNo));
				
				BasicDBObject update = null , where = null;
				switch (request.getRequestType()) {
				
				case MyRequest.REQUEST_TYPE_UPDATE_ACCOUNT:
					Short typeUpdate = request.getTypeUpdate();
					if(typeUpdate==null || typeUpdate == MyRequest.TYPE_NONE) {
						typeUpdate = MyRequest.TYPE_ALL;
					}
					// create update part of write based on the typeUpdate field in request
					final Account account = request.getAccount();
					update = new BasicDBObject();
					// for type of account info only update fields of AccountInfo
					if(typeUpdate == MyRequest.TYPE_ONLY_ACCOUNT_INFO || 
							typeUpdate == MyRequest.TYPE_ALL) {
						AccountInfo info = account.getAccountInfo();
						update = update.append("accountInfo.createdAt", info==null ? "" : 
								Config.getNotNull(info.getCreatedAt()))
								.append("accountInfo.type", info==null ? "" : Config.getNotNull(info.getType()))
								.append("accountInfo.currency", info==null ? "" : Config.getNotNull(info.getCurrency()));
					}
					// for type of personal info only update fields of PersonalInfo
					if(typeUpdate == MyRequest.TYPE_ONLY_PERSONAL_INFO || 
							typeUpdate == MyRequest.TYPE_ALL) {
						PersonalInfo info = account.getPersonalInfo();
						update = update.append("personalInfo.firstname", info==null ? "" : 
								Config.getNotNull(info.getFirstname()))
								.append("personalInfo.lastname", info==null ? "" : 
									Config.getNotNull(info.getLastname()))
								.append("personalInfo.birthdate", info==null ? "" : 
									Config.getNotNull(info.getBirthdate()))
								.append("personalInfo.passportNo", info==null ? "" : 
									Config.getNotNull(info.getPassportNo()))
								.append("personalInfo.address", info==null ? "" : 
									Config.getNotNull(info.getAddress()))
								.append("personalInfo.email", info==null ? "" : 
									Config.getNotNull(info.getEmail()))
								.append("personalInfo.phoneNumber", info==null ? "" : 
									Config.getNotNull(info.getPhoneNumber()))
								.append("personalInfo.mobileNumber", info==null ? "" : 
									Config.getNotNull(info.getMobileNumber()));
					}
					// only update balance field
					if(typeUpdate == MyRequest.TYPE_ONLY_BALANCE || 
							typeUpdate == MyRequest.TYPE_ALL) {
						update = update.append("balance", Config.getNotNull(account.getBalance()));
					}
					update = new BasicDBObject().append("$set", update);
					where = new BasicDBObject().append("$and", andList);
					writes.add(new UpdateOneModel<Account>(where, 
							update, new UpdateOptions().upsert(false)));
					break;
				
				case MyRequest.REQUEST_TYPE_DEPOSIT:
					if(andList.size()==1) {
						where = andList.get(0);
					}
					else {
						where = new BasicDBObject().append("$and", andList);
					}
					// increment balance
					update = new BasicDBObject().append("$inc", 
							new BasicDBObject().append("balance", Config.getNotNull(amount)));
					writes.add(new UpdateOneModel<Account>(where, 
							update, new UpdateOptions().upsert(false)));
					break;
				
				case MyRequest.REQUEST_TYPE_WITHDRAWAL:
					// balance should be greater than amount
					andList.add(new BasicDBObject().append("balance", 
							new BasicDBObject().append("$gt", Config.getNotNull(amount))));
					where = new BasicDBObject().append("$and", andList);
					// decrement balance
					update = new BasicDBObject().append("$inc", 
							new BasicDBObject().append("balance", -1 * Config.getNotNull(amount)));
					writes.add(new UpdateOneModel<Account>(where, 
							update, new UpdateOptions().upsert(false)));
					break;
				
				case MyRequest.REQUEST_TYPE_TRANSFER:
					// update source account
					// balance should be greater than amount
					if(request.getAccount().getBalance() > amount) {
						final CurrencyMap currencyMap = list.get(request);
						final double rate = (currencyMap == null ? 1d : currencyMap.getRate());
						logger.debug("currencyMap = "+currencyMap);
						
						if(andList.size()==1) {
							where = andList.get(0);
						}
						else {
							where = new BasicDBObject().append("$and", andList);
						}
						
						// decrement balance of source account
						update = new BasicDBObject().append("$inc", 
								new BasicDBObject().append("balance", -1 * Config.getNotNull(amount)));
						writes.add(new UpdateOneModel<Account>(where, 
								update, new UpdateOptions().upsert(false)));
						
						// update balance of destination account
						where = new BasicDBObject().append("accountInfo.accountNo", 
								request.getFinancialAccountNo2());
						// increment its balance
						update = new BasicDBObject().append("$inc", 
							new BasicDBObject().append("balance", rate * Config.getNotNull(amount)));
						writes.add(new UpdateOneModel<Account>(where, 
							update, new UpdateOptions().upsert(false)));
					}
					break;

				default:
					return;
				}
			}
		});
		final BulkWriteOptions options = new BulkWriteOptions();
		options.ordered(false);
		options.bypassDocumentValidation(true);
		if(writes.size()>0) {
			this.accountCollection.bulkWrite(writes, options);
		}
		
		// return updated results by reading it from database
		final List<Account> accountList = new ArrayList<Account>();
		for(MyRequest r : list.keySet()) {
			accountList.add(r.getAccount());
			if(r.getRequestType()==MyRequest.REQUEST_TYPE_TRANSFER) {
				accountList.add(AccountFactory.account(r.getFinancialAccountNo2()));	
			}
		}
		return this.batchRead(accountList);
	}
	
	/**
	 * insert a single log object
	 */
	public void insertLog(LogEntity log) {
		if(log.getTimestamp()==null) {
			log.setTimestamp(System.currentTimeMillis());
		}
		try {
			// convert log object to json
			final StringWriter out = new StringWriter();
			this.jsonMapper.writeValue(out, log);
			this.logCollection.insertOne(new Document().append("time", 
					log.getTimestamp().longValue()).append("transaction", 
							log.getTransactionID().longValue()).append("data", out.toString()));
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * insert multiple logs in one operation
	 */
	public void insertLog(Collection<LogEntity> list) {
		for(LogEntity log : list) {
			if(log.getTimestamp()==null) {
				log.setTimestamp(System.currentTimeMillis());
			}
		}
		try {
			final InsertManyOptions options = new InsertManyOptions();
			options.ordered(false);
			options.bypassDocumentValidation(true);
			final List<Document> docs = new ArrayList<Document>();
			for(LogEntity log : list) {
				final StringWriter out = new StringWriter();
				this.jsonMapper.writeValue(out, log);
				// define some fields to be able to perform search
				docs.add(new Document()
						.append("time", log.getTimestamp().longValue())
						.append("transaction", log.getTransactionID().longValue())
						.append("data", out.toString())
						.append("account", log.getAccountNo())
				);
			}
			this.logCollection.insertMany(docs, options);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * search logs.
	 * perform AND if more than one input are given.
	 * @param asc - asc order of results by time
	 * @param startTime - time of logs should be after this time- nullable
	 * @param endTime - time of logs should be before this time- nullable
	 * @param transaction - transactionID - nullable
	 * @param accountNo - accountNo of log - nullable
	 * @param consumer - to iterate over results
	 */
	public void searchLog(final boolean asc, final Long startTime, 
			final Long endTime, final Long transaction, final String accountNo,
			final Consumer<LogEntity> consumer) {
		// create and query
		BasicDBObject filter = new BasicDBObject();
		if(startTime!=null) {
			filter	= filter.append("time", 
					new BasicDBObject().append("$gte", startTime.longValue()));
		}
		if(endTime!=null) {
			filter = filter.append("time", 
					new BasicDBObject().append("$lte", endTime.longValue()));
		}
		if(transaction!=null) {
			filter = filter.append("transaction", transaction.longValue());
		}
		if(accountNo!=null) {
			filter = filter.append("account", accountNo);
		}
		this.logCollection.find(filter).projection(Projections.exclude(
				"_id", "time", "transaction", "account")).
			sort(asc ? Sorts.ascending("time") : Sorts.descending("time")).forEach(
					new Consumer<Document>() {
			@Override
			public void accept(Document d) {
				try {
					final StringReader in = new StringReader(d.get("data").toString());
					final int type = (Document.parse(d.get("data").toString())).getInteger("type", 0);
					// detect the type of the log object by its type
					LogEntity log;
					if(type==LogEntity.TYPE_CREATE_ACCOUNT || type==LogEntity.TYPE_UPDATE_ACCOUNT) {
						log = jsonMapper.readValue(in, UpdateLogEntity.class);
					}
					else if(type==LogEntity.TYPE_DEPOSIT || type==LogEntity.TYPE_WITHDRAWAL) {
						log = jsonMapper.readValue(in, FinancialLogEntity.class);
					}
					else if(type==LogEntity.TYPE_TRANSFER) {
						log = jsonMapper.readValue(in, TransferLogEntity.class);
					}
					else {
						log = jsonMapper.readValue(in, LogEntity.class);
					}
					consumer.accept(log);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});;
	}
	
	/**
	 * delete all the logs
	 */
	public void deleteAllLogs(){
		this.logCollection.deleteMany(new BasicDBObject());
	}
	
}
