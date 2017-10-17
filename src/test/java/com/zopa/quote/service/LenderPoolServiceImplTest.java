package com.zopa.quote.service;

import static org.junit.Assert.*;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zopa.quote.config.AppConfig;
import com.zopa.quote.domain.Lender;
import com.zopa.quote.repository.LenderPoolRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class LenderPoolServiceImplTest {
	
	@Autowired private LenderPoolRepository lenderPoolRepository;
	@Autowired private LenderPoolService lenderPoolService;
	
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
	private Lender lender1;
	private Lender lender2;
	private Lender lender3;
	private Lender lender4;
	

	@Before
	public void setUp() throws Exception {
		currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "GB"));
		
		lender1 = new Lender(1, "Lender #1", 0.066, 1000.0);
		lenderPoolRepository.save(lender1);
		
		lender2 = new Lender(2, "Lender #2", 0.075, 2000.0);
		lenderPoolRepository.save(lender2);
		
		lender3 = new Lender(3, "Lender #3", 0.070, 10000.0);
		lenderPoolRepository.save(lender3);
		
		lender4 = new Lender(4, "Lender #4", 0.072, 1000.0);
		lenderPoolRepository.save(lender4);
	}

	@Test
	public void testCalculateQuote() {
		double amountToBorrow = 1050;
		String stringResult = lenderPoolService.calculateQuote(amountToBorrow);
		assertTrue(String.format(errorMessageValueNotAllowed, currencyFormatter.format(incrementValueAllowed), currencyFormatter.format(amountToBorrow)).equals(stringResult));
		
		amountToBorrow = 900;
		stringResult = lenderPoolService.calculateQuote(amountToBorrow);
		assertTrue(String.format(errorMessageMinimumValueAllowed, currencyFormatter.format(minimumValueAllowed), currencyFormatter.format(amountToBorrow)).equals(stringResult));
		
		amountToBorrow = 15100;
		stringResult = lenderPoolService.calculateQuote(amountToBorrow);
		assertTrue(String.format(errorMessageMaximumValueAllowed, currencyFormatter.format(maximumValueAllowed), currencyFormatter.format(amountToBorrow)).equals(stringResult));
		
		amountToBorrow = 15000;
		stringResult = lenderPoolService.calculateQuote(amountToBorrow);
		assertTrue(String.format(errorMessageAmountUnfeasible, currencyFormatter.format(amountToBorrow)).equals(stringResult));
	}

	@Test
	public void testIsAmountFeasible() {
		assertTrue(lenderPoolService.isAmountFeasible(2000));
		assertTrue(lenderPoolService.isAmountFeasible(4000));
		assertFalse(lenderPoolService.isAmountFeasible(35000));
	}

	@Test
	public void testIsAmountAllowed() {
		assertTrue(lenderPoolService.isAmountAllowed(100));
		assertTrue(lenderPoolService.isAmountAllowed(1000));
		assertTrue(lenderPoolService.isAmountAllowed(1100));
		assertFalse(lenderPoolService.isAmountAllowed(1150));
		assertFalse(lenderPoolService.isAmountAllowed(1105));
		assertFalse(lenderPoolService.isAmountAllowed(1001));
	}

	@Test
	public void testIsAmountOverOrEqualsMinimum() {
		assertTrue(lenderPoolService.isAmountGreaterOrEqualsThanMinimum(1000));
		assertTrue(lenderPoolService.isAmountGreaterOrEqualsThanMinimum(1100));
		assertFalse(lenderPoolService.isAmountGreaterOrEqualsThanMinimum(999));
		assertFalse(lenderPoolService.isAmountGreaterOrEqualsThanMinimum(900));
	}

	@Test
	public void testIsAmountUnderOrEqualsMaximum() {
		assertTrue(lenderPoolService.isAmountLowerOrEqualsThanMaximum(14900));
		assertTrue(lenderPoolService.isAmountLowerOrEqualsThanMaximum(15000));
		assertFalse(lenderPoolService.isAmountLowerOrEqualsThanMaximum(15001));
		assertFalse(lenderPoolService.isAmountLowerOrEqualsThanMaximum(15100));
	}

	@Test
	public void testGetLowestRateLenders() {
		List<Lender> lenderPool = lenderPoolService.getLowestRateLenders(2000);
		assertTrue(lenderPool.contains(lender1));
		assertTrue(lenderPool.contains(lender3));
		assertFalse(lenderPool.contains(lender2));
		assertFalse(lenderPool.contains(lender4));
	}
	
	@Test
	public void TestCalculateMonthlyRepayment() {
		double monthlyRepayment = lenderPoolService.calculateMonthlyRepayment(lenderPoolService.getLowestRateLenders(1000), 1000);
		assertTrue(30.694539428781287 == monthlyRepayment);
		
		monthlyRepayment = lenderPoolService.calculateMonthlyRepayment(lenderPoolService.getLowestRateLenders(5000), 5000);
		assertTrue(154.20292689026866 == monthlyRepayment);
	}
	
	@Test
	public void TestCalculateTotalRepayment() {
		double totalRepayment = lenderPoolService.calculateTotalRepayment(lenderPoolService.getLowestRateLenders(1000), 1000);
		System.out.println(totalRepayment);
		assertTrue(1218.301271811185 == totalRepayment);
		
		totalRepayment = lenderPoolService.calculateTotalRepayment(lenderPoolService.getLowestRateLenders(5000), 5000);
		System.out.println(totalRepayment);
		assertTrue(6150.003621718897 == totalRepayment);
	}

}
