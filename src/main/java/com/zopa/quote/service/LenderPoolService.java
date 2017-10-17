package com.zopa.quote.service;

import java.util.List;

import com.zopa.quote.domain.Lender;

public interface LenderPoolService {
	
	String calculateQuote(double amountToBorrow);
	boolean isAmountFeasible(double amountToBorrow);
	boolean isAmountAllowed(double amountToBorrow);
	boolean isAmountGreaterOrEqualsThanMinimum(double amountToBorrow);
	boolean isAmountLowerOrEqualsThanMaximum(double amountToBorrow);
	List<Lender> getLowestRateLenders(double amountToBorrow);
	double calculateMonthlyRepayment(List<Lender> lendersPool, double amountToBorrow);
	double calculateTotalRepayment(List<Lender> lendersPool, double amountToBorrow);
	double calculateAverageRate(List<Lender> lendersPool);
	
}
