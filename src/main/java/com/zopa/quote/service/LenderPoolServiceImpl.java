package com.zopa.quote.service;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zopa.quote.domain.Lender;
import com.zopa.quote.repository.LenderPoolRepository;

@Service
public class LenderPoolServiceImpl implements LenderPoolService {
	
	@Autowired private LenderPoolRepository poolLenderRepository;
	
	@Value( "${message.error.valueNotAllowed}" )
	private String errorMessageValueNotAllowed;
	
	@Value( "${message.error.amountUnfeasible}" )
	private String errorMessageAmountUnfeasible;
	
	@Value( "${message.error.minimumValueAllowed}" )
	private String errorMessageMinimumValueAllowed;
	
	@Value( "${message.error.maximumValueAllowed}" )
	private String errorMessageMaximumValueAllowed;
	
	@Value( "${message.quote}" )
	private String messageQuote;
	
	@Value( "${loan.amount.increment.value.allowed}" )
	private double incrementValueAllowed;
	
	@Value( "${loan.amount.minimum.value.allowed}" )
	private double minimumValueAllowed;
	
	@Value( "${loan.amount.maximum.value.allowed}" )
	private double maximumValueAllowed;
	
	@Value( "${loan.time.value.allowed}" )
	private double timeValueAllowed;
	
	private NumberFormat currencyFormatter;
	

	public LenderPoolServiceImpl() {
		currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "GB"));
	}
	
	
	/**
	 * Process quote request.  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      String with the result of calculation.
	 */
	@Override
	public String calculateQuote(double amountToBorrow) {
		
		//Validates if amount to borrow is a valid value
		if(!isAmountAllowed(amountToBorrow)) {
			return String.format(errorMessageValueNotAllowed, currencyFormatter.format(incrementValueAllowed), currencyFormatter.format(amountToBorrow)); 
		}
		
		//Validates if amount to borrow is over minimum value allowed
		if(!isAmountGreaterOrEqualsThanMinimum(amountToBorrow)) {
			return String.format(errorMessageMinimumValueAllowed, currencyFormatter.format(minimumValueAllowed), currencyFormatter.format(amountToBorrow));
		}
		
		//Validates if amount to borrow is under maximum value allowed
		if(!isAmountLowerOrEqualsThanMaximum(amountToBorrow)) {
			return String.format(errorMessageMaximumValueAllowed, currencyFormatter.format(maximumValueAllowed), currencyFormatter.format(amountToBorrow));
		}
		
		//Validates if amount to borrow could be covered by lender pool
		if(!isAmountFeasible(amountToBorrow)) {
			return String.format(errorMessageAmountUnfeasible, currencyFormatter.format(amountToBorrow)); 
		}

		//Return lender list with the lowest rate available that covers the amount to borrow
		List<Lender> lenderPool = getLowestRateLenders(amountToBorrow);
		
		//Return monthly repayment based on lender pool list
		double monthlyRepayment = calculateMonthlyRepayment(new LinkedList<>(lenderPool), amountToBorrow);
		
		//Return total repayment based on lender pool list
		double totalRepayment = calculateTotalRepayment(new LinkedList<>(lenderPool), amountToBorrow);
		
		//Return rate average based on lender pool list (this was assumed, no requirement found)
		double rate = calculateAverageRate(new LinkedList<>(lenderPool));
		rate *= 100;
		
		return String.format(messageQuote, currencyFormatter.format(amountToBorrow), rate, currencyFormatter.format(monthlyRepayment), currencyFormatter.format(totalRepayment));
	}
	
	
	/**
	 * Returns true only if amount to borrow requested, can be covered by the 
	 * available amounts from lender pool. 
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      TRUE if amount requested can be covered by lenders, FALSE if it cannot
	 */
	@Override
	public boolean isAmountFeasible(double amountToBorrow) {
		
		//Get all lenders
		List<Lender> lenderPool = poolLenderRepository.findAll();
		double amountCovered = 0.0;
		
		//Loop lender list
		for(Lender lender: lenderPool) {
			
			//Totalize lender's available
			amountCovered += lender.getAvailable();
			
			//Validates if amount to borrow is covered, if covered interrupt loop
			if(amountCovered >= amountToBorrow) break;
		}
		
		//Validates if amount to borrow has been covered
		return (amountCovered >= amountToBorrow);
	}

	
	/**
	 * Returns true only if amount to borrow respects the 100 step increment  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      TRUE if amount requested respects 100 step increment, FALSE if it does not
	 */
	@Override
	public boolean isAmountAllowed(double amountToBorrow) {
		//Validates if amount to borrow is a valid step of 100
		return (amountToBorrow % incrementValueAllowed) == 0;
	}
	

	/**
	 * Returns true only if amount to borrow is EQUALS or GREATER than minimum allowed  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      TRUE if amount requested is equals or greater than minimum, FALSE if it is not
	 */
	@Override
	public boolean isAmountGreaterOrEqualsThanMinimum(double amountToBorrow) {
		//Validates if amount to borrow is greater or equals than minimum allowed
		return amountToBorrow >= minimumValueAllowed;
	}
	

	/**
	 * Returns true only if amount to borrow is EQUALS or LOWER than maximum allowed  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      TRUE if amount requested is equals or lower than maximum, FALSE if it is not
	 */
	@Override
	public boolean isAmountLowerOrEqualsThanMaximum(double amountToBorrow) {
		//Validates if amount to borrow is lower or equals than maximum allowed
		return amountToBorrow <= maximumValueAllowed;
	}

	
	/**
	 * Returns the list of lenders with lowest rate available that covers the amount to borrow.  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @return      List of lender objects with lowest rate available for amount requested.
	 */
	@Override
	public List<Lender> getLowestRateLenders(double amountToBorrow) {
		
		//Instantiate lender list with lowest rates
		List<Lender> competitiveLenderPool = new LinkedList<>();
		//Get all lenders from repository sorted by rate ascending
		List<Lender> lenderPool = poolLenderRepository.findAllSorted();
		double amountCovered = 0.0;
		
		//Loop lender list
		for(Lender lender: lenderPool) {
			//Validates if amount to borrow is covered, if covered interrupt loop
			if(amountCovered >= amountToBorrow) break;
			
			//Add as competitive lender 
			competitiveLenderPool.add(lender);
			
			//Add amount covered
			amountCovered += lender.getAvailable();
		}
		
		return competitiveLenderPool;
	}
	
	
	/**
	 * Method calculates monthly repayment based on lender list provided.  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @param  lenderPool list of lenders to process
	 * @return      monthly repayment
	 */
	@Override
	public double calculateMonthlyRepayment(List<Lender> lenderPool, double amountToBorrow) {
		
		return calculateMonthlyRepayment(lenderPool, amountToBorrow, 0);
		
	}

	private double calculateMonthlyRepayment(List<Lender> lenderPool, double amountToBorrow, double loanAmountCovered) {
		
		//Validates if amount to borrow is covered and lenders still to evaluate
		if(loanAmountCovered < amountToBorrow && lenderPool.size() > 0) {
			
			//Get next lender from list
			Lender lender = lenderPool.remove(0);
			
			//Validates if lender has available to cover total amount to borrow
			if((loanAmountCovered + lender.getAvailable()) >= amountToBorrow) {
				
				//Lender has enough to borrow, process lender and return
				return calculateMonthlyRepaymentByLender(lender, amountToBorrow - loanAmountCovered);
				
			} else {
				
				//Not enough to covered all, process lender and pass the lender pool to process
				loanAmountCovered += lender.getAvailable();
				double monthlyRepayment = calculateMonthlyRepaymentByLender(lender, lender.getAvailable());
				return monthlyRepayment += calculateMonthlyRepayment(lenderPool, amountToBorrow, loanAmountCovered);
				
			}
		}
		
		return 0;
	}
	
	
	private double calculateMonthlyRepaymentByLender(Lender lender, double amountToBorrow) {
		
		// Formula used -> Monthly repayment with monthly APR = (month_interest * requested_amount) / (1 - (1 + month_interest) ^ -(total_months))
		return ( ( lender.getRate()/12 ) * amountToBorrow ) / ( 1 - Math.pow( ( 1 + ( lender.getRate()/12 ) ), -timeValueAllowed ) );
		
	}

	
	/**
	 * Method calculates total repayment based on lender list provided.  
	 *
	 * @param  amountToBorrow amount requested by the user
	 * @param  lenderPool list of lenders to process
	 * @return      total repayment
	 */
	@Override
	public double calculateTotalRepayment(List<Lender> lenderPool, double amountToBorrow) {
	
		return calculateTotalRepayment(lenderPool, amountToBorrow, 0);
		
	}

	private double calculateTotalRepayment(List<Lender> lenderPool, double amountToBorrow, double loanAmountCovered) {
		
		//Validates if amount to borrow is covered and lenders still to evaluate
		if(loanAmountCovered < amountToBorrow && lenderPool.size() > 0) {
			
			//Get next lender from list
			Lender lender = lenderPool.remove(0);
			
			//Validates if lender has available to cover total amount to borrow
			if((loanAmountCovered + lender.getAvailable()) >= amountToBorrow) {
				
				//Lender has enough to borrow, process lender and return
				return calculateTotalRepaymentByLender(lender, amountToBorrow - loanAmountCovered);
				
			} else {

				//Not enough to covered all, process lender and pass the lender pool to process
				loanAmountCovered += lender.getAvailable();
				double monthlyRepayment = calculateTotalRepaymentByLender(lender, lender.getAvailable());
				return monthlyRepayment += calculateTotalRepayment(lenderPool, amountToBorrow, loanAmountCovered);
				
			}
		}
		
		return 0;
	}
	
	private double calculateTotalRepaymentByLender(Lender lender, double amountToBorrow) {
		
		// Formula used -> Total repayment with monthly APR = requested_amount * (1 + month_interest) ^ total_months
		return ( amountToBorrow * Math.pow( ( 1 + ( lender.getRate()/12 ) ), timeValueAllowed ) );
		
	}

	
	/**
	 * Method calculates rate average based on lender list provided.  
	 *
	 * @param  lenderPool list of lenders to process
	 * @return      rate average
	 */
	@Override
	public double calculateAverageRate(List<Lender> lendersPool) {
		
		double totalRates = 0;
		for(Lender lender: lendersPool) {
			totalRates += lender.getRate();
		}
		
		return (totalRates / lendersPool.size());
		
	}
	
	
	
}
