package com.zopa.quote;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.zopa.quote.config.BatchConfig;
import com.zopa.quote.domain.Lender;
import com.zopa.quote.repository.LenderPoolRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BatchConfig.class })
public class ZopaQuoteTests {

	@Autowired private LenderPoolRepository lenderPoolRepository;
	@Autowired private FlatFileItemReader<Lender> flatFileItemReader;
	@Autowired private JobLauncher jobLauncher;
	@Autowired private JobBuilder jobBuilder;
	@Autowired private Step step;
	

	@Before
	public void setUp() throws Exception {
		
		try {
			
			flatFileItemReader.setResource(new ClassPathResource("sample-data.csv"));
			jobBuilder.incrementer(new RunIdIncrementer());
			Job loadLendersJob = jobBuilder.start(step).listener(new JobExecutionListener() {
				
				@Override
				public void beforeJob(JobExecution arg0) {}
				
				@Override
				public void afterJob(JobExecution jobExecution) {}
				
			}).build();
			
			JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
			jobLauncher.run(loadLendersJob, jobParameters);
			
		} catch (Exception e) {
			
			System.out.println(e);
			
		}
	}

	@Test
	public void testLendersBatchLoad() {
		
		assertTrue(7 == lenderPoolRepository.total());
		
		List<Lender> lenderPool = lenderPoolRepository.findAllSorted();
		assertTrue(lenderPool.get(0).getRate() == 0.069);
		assertTrue(lenderPool.get(1).getRate() == 0.071);
		assertTrue(lenderPool.get(2).getRate() == 0.071);
		assertTrue(lenderPool.get(3).getRate() == 0.074);
		assertTrue(lenderPool.get(4).getRate() == 0.075);
		assertTrue(lenderPool.get(5).getRate() == 0.081);
		assertTrue(lenderPool.get(6).getRate() == 0.104);
	
	}

}
