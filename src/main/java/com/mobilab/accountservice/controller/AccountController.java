package com.mobilab.accountservice.controller;

import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.ActionTransaction;
import com.mobilab.accountservice.entities.ApiResponse;
import com.mobilab.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/accounts")
@ConditionalOnProperty("my.rest")
public class AccountController {
	@Autowired
	private AccountService accountService;
	/**
	 * Returns all created accounts
	 *
	 * @return - Api response {accounts in data part in json array format}
	 */
	@GetMapping
	public ApiResponse findAll() {
		return accountService.readAllAccounts();
	}
	/**
	 * Returns account information for an existing account number
	 *
	 * @param id - accountNo.
	 * @return - ApiResponse with accounts information in data field
	 */
	@GetMapping("/{id}")
	public ApiResponse findOne(@PathVariable String id) {
		return accountService.readAccount(id);
	}
	/**
	 * Creates New account and returns transaction id and transaction path url to see the results
	 *
	 * @param account - New Account Information
	 * @param request - Http servlet request which can be used for extracting sender ip
	 * @return - ApiResponse (transaction id and transaction result path)
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse create(@Valid @RequestBody Account account, HttpServletRequest request) {
		return accountService.createNewAccount(account, request.getRemoteAddr());
	}
	/**
	 * Updates existing account and returns transaction id and transaction path url to see the results
	 *
	 * @param account - Accounts to be updated information
	 * @param  accountNo - Number of account to be updated
	 * @param request - Http servlet request which can be used for extracting sender ip
	 * @return - ApiResponse (transaction id and transaction result path)
	 */
	@PutMapping("/{accountNo}")
	public ApiResponse updateAccount(@RequestBody Account account, @PathVariable String accountNo, HttpServletRequest request) {
		return accountService.updateAccount(accountNo, account, request.getRemoteAddr());
	}

	/**
	 * Handles financial transactions include deposit, withdrawal and transfer
	 *
	 * @param action - Financial Transaction information
	 * @param  accountNo - Number of source account for financial transaction
	 * @param request - Http servlet request which can be used for extracting sender ip
	 * @return - ApiResponse (transaction id and transaction result path)
	 */
	@PostMapping("/{accountNo}")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse handleTransaction(@RequestBody ActionTransaction action,
			@PathVariable String accountNo, HttpServletRequest request) {
		return accountService.handleAction(action, accountNo, request.getRemoteAddr());
	}

}
