package com.mobilab.accountservice.bussiness.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.log4j.Logger;

/**
 * a utility to access the name of the topics and create them if not exist
 */
public class TopicUtils {
	
	final static Logger logger = Logger.getLogger(TopicUtils.class);

	public static final String TOPIC_DB_REQUEST = "t_db_request";
	public static final String TOPIC_DB_RESPONSE = "t_db_response";
	
	/**
	 * create topics if not exist.
	 * read configuration from those configs start with 'kafkatopic.'.
	 * for example, retention and number of partitions.
	 */
	public static void initTopics() throws Exception {
		ZkClient zkClient = null;
		try {
			final int replication = Config.getIntProperty("topic_replication", 1);
			final int partitions = Config.getIntProperty("topic_partitions", 3);
			final String zookeeperHost = Config.getStringProperty("zookeeper_host", "127.0.0.1:2181");
			final int sessionTimeOut = 40 * 1000;
			final int connectionTimeOut = 20 * 1000;
			final Properties topicConfiguration = new Properties();
			Config.getConfig("kafkatopic.").forEach(new BiConsumer<Object, Object>() {
				@Override
				public void accept(Object key, Object value) {
					topicConfiguration.put(key.toString(), value);
				}
			});
			logger.info(String.format("Initiating Topics , zookeeper host = %s, properties = %s", zookeeperHost,
					topicConfiguration));
			zkClient = new ZkClient(zookeeperHost, sessionTimeOut, connectionTimeOut, ZKStringSerializer$.MODULE$);
			final ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHost), false);
			final List<String> topicNameList = Arrays.asList(
					TOPIC_DB_REQUEST, TOPIC_DB_RESPONSE);
			for(String topicName : topicNameList) {
				if (AdminUtils.topicExists(zkUtils, topicName)) {
					logger.info(String.format("Topic exists = %s", topicName));
				}
				else {
					logger.info(String.format("Create topic = %s", topicName));
					AdminUtils.createTopic(zkUtils, topicName, partitions, replication, topicConfiguration,
							RackAwareMode.Disabled$.MODULE$);
				}
			}
			if (zkClient != null) {
				zkClient.close();
			}
		} catch (Exception ex) {
			logger.error("can not initialize topics");
			logger.error(ex);
			if (zkClient != null) {
				zkClient.close();
			}
			throw ex;
		}
	}

}
