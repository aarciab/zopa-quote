package com.zopa.quote.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.BindException;

import com.zopa.quote.domain.Lender;
import com.zopa.quote.repository.LenderPoolRepository;

@Configuration
@EnableBatchProcessing
@Import(AppConfig.class)
public class BatchConfig {
	
	private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);
	
	@Autowired private LenderPoolRepository poolLenderRepository;
	@Autowired private JobBuilderFactory jobBuilderFactory;
    @Autowired private StepBuilderFactory stepBuilderFactory;
    
    private int rowSequence = 0;
   

    @Bean
    public JobRepository jobRepository() throws Exception {
    	MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(new ResourcelessTransactionManager());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
    
    @Bean
    public JobBuilder loadLendersJobBuilder() {
    	return jobBuilderFactory.get("loadLendersJob");
    }
    
    @Bean
    protected Step loadLendersStep() {
        return stepBuilderFactory.get("loadLendersStep")
            .<Lender, Lender> chunk(1000)
            .reader(csvFileItemReader())
            .writer(csvFileItemWriter())
            .build();
    }
    
    @Bean
    public FlatFileItemReader<Lender> csvFileItemReader() {
    	
    	DelimitedLineTokenizer dlt = new DelimitedLineTokenizer();
    	dlt.setDelimiter(",");
    	DefaultLineMapper<Lender> dlm = new DefaultLineMapper<Lender>();
    	dlm.setLineTokenizer(dlt);
    	dlm.setFieldSetMapper(new FieldSetMapper<Lender>() {

			@Override
			public Lender mapFieldSet(FieldSet fieldSet) throws BindException {
				
				//Validates each value from CSV, in case of error default value set
				String name = "";
				try {
					name = fieldSet.readString(0);
				} catch (Exception e) {
					log.error("Error parsing NAME: " + e.getMessage());
					name = null;
				}
				
				double rate = 0;
				try {
					rate = fieldSet.readDouble(1);
				} catch (Exception e) {
					log.error("Error parsing RATE: " + e.getMessage());
					rate = 0;
				}
				
				double available = 0;
				try {
					available = fieldSet.readDouble(2); 
				} catch (Exception e) {
					log.error("Error parsing AVAILABLE: " + e.getMessage());
					available = 0;
				}
	    		
	    		return new Lender(++rowSequence, name, rate, available);
	    		
			}
    		
    	});
    	
        FlatFileItemReader<Lender> csvFileItemReader = new FlatFileItemReader<Lender>();
        csvFileItemReader.setLineMapper(dlm);
        return csvFileItemReader;
    }
    
    @Bean
    public ItemWriter<Lender> csvFileItemWriter() {
    	return new ItemWriter<Lender>() {

			@Override
			public void write(List<? extends Lender> lenderList) throws Exception {
				
				//All parsed records to repository
				for(Lender lender: lenderList) {
					
					//Ignoring parsed records with default values
					if(lender.getName() != null && lender.getAvailable() > 0) {
						poolLenderRepository.save(lender);
					}
					
				}
				
			}
    		
    	};
    }
    
}
