package com.zopa.quote.repository;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zopa.quote.config.AppConfig;
import com.zopa.quote.domain.Lender;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class LenderPoolRepositoryImplTest {
	
	@Autowired private LenderPoolRepository lenderPoolRepository;
	private Lender lender1;
	private Lender lender2;
	private Lender lender3;
	
	@Before
	public void setUp() throws Exception {
		lender1 = new Lender(1, "Lender #1", 0.069, 1400.0);
		lenderPoolRepository.save(lender1);
		
		lender2 = new Lender(2, "Lender #2", 0.071, 1500.0);
		lenderPoolRepository.save(lender2);
		
		lender3 = new Lender(3, "Lender #3", 0.070, 1200.0);
		lenderPoolRepository.save(lender3);
	}

	@Test
	public void testSave() {
		Lender lender4 = new Lender(4, "Lender #4", 0.072, 1000.0);
		lenderPoolRepository.save(lender4);
		assertEquals(4, lenderPoolRepository.total());
	}

	@Test
	public void testFind() {
		Lender lenderFound = lenderPoolRepository.find(lender1.getId());
		assertEquals(lender1, lenderFound);		
	}

	@Test
	public void testFindAll() {
		List<Lender> lendersPool = lenderPoolRepository.findAll();
		assertEquals(3, lenderPoolRepository.total());
		assertEquals(3, lendersPool.size());
	}

	@Test
	public void testFindAllSorted() {
		List<Lender> lendersFound = lenderPoolRepository.findAllSorted();
		double hightestRateFound = 0;
		for(Lender lender: lendersFound){
			assertTrue(hightestRateFound < lender.getRate());
			hightestRateFound = lender.getRate();
		}
	}

	/*@Test
	public void testFindAllSortedBy() {
		List<Lender> lendersFound = poolLenderRepository.findAllSortedBy(Lender.getAvailabilitySorter());
		double hightestAmountAvailableFound = 0;
		for(Lender lender: lendersFound){
			assertTrue(hightestAmountAvailableFound < lender.getAvailable());
			hightestAmountAvailableFound = lender.getAvailable();
		}
	}*/

	@Test
	public void testFlush() {
		lenderPoolRepository.flush();
		assertEquals(0, lenderPoolRepository.total());
	}

}
