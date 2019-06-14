package com.mobilab.accountservice.service;

import com.mobilab.accountservice.bussiness.message.AccountFactory;
import com.mobilab.accountservice.bussiness.message.MyResponse;
import com.mobilab.accountservice.bussiness.process.Database;
import com.mobilab.accountservice.bussiness.utils.CommonUtils;
import com.mobilab.accountservice.entities.Account;
import com.mobilab.accountservice.entities.ApiResponse;
import com.mobilab.accountservice.entities.ApiResponseWithData;

import com.mobilab.accountservice.entities.FinancialLogEntity;
import com.mobilab.accountservice.entities.LogEntity;
import com.mobilab.accountservice.entities.LogRequest;
import com.mobilab.accountservice.entities.UpdateLogEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty("my.rest")
public class TransactionResultService {
	final static Logger logger = Logger.getLogger(TransactionResultService.class);
	@Autowired @Qualifier("database") Database databaseRepo;
	private Map<Long, MyResponse> receivedResponses = Collections.synchronizedMap(new HashMap<Long, MyResponse>());
	/**
	 * Method which are used by response gateway to share messages with rest part
	 *
	 */
	public void put(MyResponse message) {
		logger.debug("Receive message = " + message);
		synchronized (this.receivedResponses) {
			this.receivedResponses.put(message.getId(), message);
		}
	}
	/**
	 * This method is used to search response gateway for transaction result, It can be replaced by memory caches to achieve better performance
	 *
	 */
	private MyResponse findInResponsesMap(Long key) {
		synchronized (this.receivedResponses) {
			MyResponse res = this.receivedResponses.get(key);
			logger.debug(String.format("Find message response , id = %d, message = %s", key, res));
			return res;
		}
	}
	/**
	 * Service method used for retrieving transaction result
	 * It first looks at response gateway cache and if there is no response there, then it will connect to the db and read logs for that transaction id
	 * @param tid - transaction id
	 * @return - ApiResponse (transaction result details)
	 */
	public ApiResponse getResponseForTID(long tid) {
		MyResponse response = findInResponsesMap(tid);
		ApiResponse apiResponse = new ApiResponse();
		logger.debug(String.format("tid = %d , response = %s", tid, response));
		if (response != null) {
			apiResponse = new ApiResponseWithData<Account>(response.getAccount(), tid);
			apiResponse.setError(response.getStatus() != MyResponse.STATUS_OK);
			apiResponse.setErrorMessage(response.getError());
			logger.debug("apiResponse = " + apiResponse);
		} else {
			List<LogEntity> logs = serachLogs(true, null, null, tid, null);
			if (logs.size() > 0) {
				LogEntity logEntity = logs.get(0);
				switch (logEntity.getType()) {
					case 5:
					case 6:
						UpdateLogEntity updateLogEntity = (UpdateLogEntity) logEntity;
						apiResponse = new ApiResponseWithData<Account>(updateLogEntity.getAccountAfter(), tid);
						break;
					case 1:
					case 2:
					case 3:
						FinancialLogEntity financialLogEntity = (FinancialLogEntity) logEntity;
						apiResponse = new ApiResponseWithData<Account>(
								AccountFactory.account(financialLogEntity.getAccountNo(), financialLogEntity.getBalanceAfter()));
						break;
					default:
						apiResponse.setErrorMessage("some thing went wrong with this transaction - unknown transaction ype");
						apiResponse.setError(true);
				}
				apiResponse.setError(!logEntity.isStatus());
			} else {
				apiResponse.setErrorMessage("some thing went wrong with this transaction - check transaction id again");
				apiResponse.setError(true);
			}
		}
		return apiResponse;
	}
	/**
	 * service method that fetches transaction logs from data base ( both financial and non-financial logs)
	 * @param logRequest - log request(start, end, tid, ....)
	 * @return - ApiResponse (log result result details)
	 */
	public ApiResponse getLogs(LogRequest logRequest, String accountNo) {
		Date startDate = null, endDate = null;
		if (logRequest.isDesc() == null) {
			logRequest.setDesc(true);
		}
		if (StringUtils.isNotBlank(logRequest.getEndDate()) && StringUtils.isNotBlank(logRequest.getStartDate())) {
			startDate = CommonUtils.parseDateString(logRequest.getStartDate());
			endDate = CommonUtils.parseDateString(logRequest.getEndDate());
			if (startDate == null || endDate == null || startDate.getTime() >= endDate.getTime()) {
				return new ApiResponse("Wrong dates, check them again please...");
			}
			return new ApiResponseWithData<List<LogEntity>>(
					serachLogs(logRequest.isDesc(), startDate.getTime(), endDate.getTime(), logRequest.getTransactionId(), accountNo));
		}
		if (logRequest.getTransactionId() == null && (StringUtils.isBlank(logRequest.getStartDate()) || StringUtils.isBlank(logRequest.getEndDate()))) {
			return new ApiResponse("Wrong filter data- check your request");
		} else if (logRequest.getTransactionId() != null) {
			return new ApiResponseWithData<List<LogEntity>>(serachLogs(logRequest.isDesc(), null, null, logRequest.getTransactionId(), accountNo));
		} else {
			return new ApiResponse("Wrong dates, one of dates is incorrect");
		}
	}

	/**
	 * private  method that fetches transaction logs from data base ( both financial and non-financial logs)
	 * @param desc - order of logs
	 * @param start - timestamp of query start time
	 * @param end - timestamp of query end time
	 * @param tid - transaction id
	 * @param accountNo - Account number
	 * @return - ApiResponse (log result result details)
	 */
	private List<LogEntity> serachLogs(boolean desc, Long start, Long end, Long tid, String accountNo) {
		List<LogEntity> list = new ArrayList<>();
		databaseRepo.searchLog(!desc, start, end, tid, accountNo, new Consumer<LogEntity>() {
			@Override public void accept(LogEntity logEntity) {
				list.add(logEntity);
			}
		});
		return list;
	}

}
