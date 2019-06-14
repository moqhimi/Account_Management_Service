package com.mobilab.accountservice;

import com.mobilab.accountservice.bussiness.process.Database;
import com.mobilab.accountservice.bussiness.process.Gateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Initializing beans
 * Gateway and Data base beans
 */
@Configuration
public class GatewayConfig {
	@Bean(name="request_gateWay")
	@Lazy
	@ConditionalOnProperty("my.rest")
	Gateway kafkaRequestGetWay(){
		Gateway kafkaGateway = new Gateway(1L,true, true);
		return kafkaGateway;

	}

	@Value("${mongodb_host}")
	private String host;
	@Value("${mongodb_username}")
	private  String username;
	@Value("${mongodb_password}")
	private  String password;


	@Bean(name="database")
	@Lazy
	@ConditionalOnProperty("my.rest")
	Database DataBaseBean(){
		Database database= new Database(host,username, password);
		database.connect();
		return database;
	}
}

