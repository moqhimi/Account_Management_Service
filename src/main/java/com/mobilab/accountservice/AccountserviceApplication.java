package com.mobilab.accountservice;

import com.mobilab.accountservice.bussiness.utils.Config;
import com.mobilab.accountservice.testcase.StressTest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


import com.mobilab.accountservice.bussiness.process.BusinessHandler;
import com.mobilab.accountservice.bussiness.utils.NodeUtils;
import com.mobilab.accountservice.testcase.ConsistencyTest;

@EntityScan("com.mobilab.accountservice.entities")
@SpringBootApplication
public class AccountserviceApplication {

	public static void main(String[] args) {
		String command = new String();
		command = overrideConfig(args);
		SpringApplication.run(AccountserviceApplication.class, args);
		if (StringUtils.isNotBlank(command)) {
			handleConfiguration(command, args);
		}
	}

	private static String overrideConfig(String[] args) {
		if (args.length > 0) {
			String command = args[0];
			for (String arg : args) {
				if (arg.contains("--")) {
					arg = arg.replace("--", "");
					String[] conf = arg.split("=");
					if (conf.length == 2) {
						if (Config.getProperty(conf[0]) != null) {
							Config.setProperty(conf[0], conf[1]);
						}
					}
				}
			}
			return command;
		}
		return "";
	}

	public static void handleConfiguration(String command, String[] args) {
		if (command.compareTo("b") == 0 || command.compareTo("business") == 0) {
			BusinessHandler b = new BusinessHandler(NodeUtils.createID());
			b.start();
		}
		 if (command.compareTo("s")==0){
				int count = 100;
				for(int i=0; i<args.length-1; i++) {
					if(args[i].compareTo("--n")==0) {
						count = Config.toInt(args[i+1], count);
					}
				}
				StressTest stressTest= new StressTest(count);
				stressTest.run();

			}
			if(command.compareTo("c")==0){
				ConsistencyTest test= new ConsistencyTest();
				test.run();

			}
	}
}








