package com.zopa.quote;

import org.springframework.batch.core.BatchStatus;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.UrlResource;

import com.zopa.quote.config.BatchConfig;
import com.zopa.quote.domain.Lender;
import com.zopa.quote.service.LenderPoolService;

public class ZopaQuote {
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static void main(String[] args) {
		try {
			
			if (args.length == 2) {
				
	        	UrlResource urlResource = new UrlResource("file", args[0]);
	        	
	        	if(urlResource.exists()) {
	        		
	        		ApplicationContext batchContext = new AnnotationConfigApplicationContext(BatchConfig.class);
	        		LenderPoolService lenderPoolService = batchContext.getBean(LenderPoolService.class);
		        	
					FlatFileItemReader<Lender> flatFileItemReader = batchContext.getBean(FlatFileItemReader.class);        	
					flatFileItemReader.setResource(urlResource);
					
					JobLauncher jobLauncher = batchContext.getBean(JobLauncher.class);
					JobBuilder jobBuilder = batchContext.getBean(JobBuilder.class);
					Step step = batchContext.getBean(Step.class);
					
					jobBuilder.incrementer(new RunIdIncrementer());
					Job loadLendersJob = jobBuilder.start(step).listener(new JobExecutionListener() {
						
						@Override
						public void beforeJob(JobExecution arg0) {}
						
						@Override
						public void afterJob(JobExecution jobExecution) {
							
							if( jobExecution.getStatus() == BatchStatus.COMPLETED ){
								
								try {
									
									double amountToBorrow = Double.parseDouble(args[1]);
									System.out.println(lenderPoolService.calculateQuote(amountToBorrow));
									
								} catch (Exception e) {
									
									System.out.println("Exception thrown: " + e.getMessage());
									
								}
								
						    } else if(jobExecution.getStatus() == BatchStatus.FAILED){
						    	
						    	System.out.println(String.format("Error during file loading process for (%s), please check file consistency and try again!", args[0]));
						    	
						    }
							
						}
						
					}).build();
					
					JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
					jobLauncher.run(loadLendersJob, jobParameters);
					
	        	} else {
	        		
	        		System.out.println("File not found, absolute path have to be provided!");
	        		
	        	}
	        	
	        } else {
	        	
	        	System.out.println("Incorrect number of params, please try again!");
	        	
	        }
			
		} catch (Exception e) {
			
			System.out.println("Exception thrown: " + e.getMessage());
			
		}
		
	}
	
}
