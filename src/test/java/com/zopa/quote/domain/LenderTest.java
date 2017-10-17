package com.zopa.quote.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class LenderTest {

	@Test
	public void testEqualsObject() {
		Lender lender1 = new Lender(1, "Lender #1", 100.0, 1000.0);
		Lender lender1Copy = new Lender(lender1.getId(), lender1.getName(), lender1.getRate(), lender1.getAvailable());
		
		assertTrue(lender1.equals(lender1Copy));
	}

	@Test
	public void testCompareTo() {
		Lender lender1 = new Lender(1, "Lender #1", 100.0, 1000.0);
		Lender lender2 = new Lender(2, "Lender #2", 200.0, 2000.0);
		
		assertEquals(-1, lender1.compareTo(lender2));
	}

}
