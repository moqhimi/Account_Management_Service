package com.mobilab.accountservice.controller;

import com.mobilab.accountservice.entities.ApiResponse;
import com.mobilab.accountservice.entities.LogRequest;
import com.mobilab.accountservice.service.TransactionResultService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/transactions")
@ConditionalOnProperty("my.rest")
public class TransactionController {
	
	final static Logger logger = Logger.getLogger(TransactionController.class);

	@Autowired
	private TransactionResultService transactionService;

	/**
	 * Get transaction result of create update and financial transactions
	 *
	 * @param id - New Account Information
	 * @return - ApiResponse (transaction result)
	 */
	@GetMapping("/{id}")
	public ApiResponse findOne(@PathVariable Long id) {
		logger.debug("id = "+id);
		return transactionService.getResponseForTID(id);
	}
	/**
	 * Reprieves all transaction logs for specific account
	 *
	 * @param logRequest - log request (start time - end time - order(asc/desc)- transaction)
	 * @param accountNo - Account Number
	 * @return - ApiResponse (transaction id and transaction result path)
	 */
	@PostMapping("/{accountNo}")
	public ApiResponse getLogs(@RequestBody LogRequest logRequest, 	@PathVariable String accountNo){
		logger.debug("recieved log request:"+logRequest);
		return transactionService.getLogs(logRequest, accountNo);
	}


}
