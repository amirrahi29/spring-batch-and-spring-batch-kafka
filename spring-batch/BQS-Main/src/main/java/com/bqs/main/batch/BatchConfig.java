package com.bqs.main.batch;

import com.bqs.main.batch.listener.S3JobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.configuration.annotation.StepScope;

@Configuration
public class BatchConfig {

    @Autowired
    private com.bqs.main.batch.reader.S3BatchReader s3BatchReader;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job patientJob(Step patientStep, S3JobExecutionListener jobListener) {
        return new JobBuilder("patientJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(patientStep)
                .build();
    }

    @Bean
    public Step patientStep(
            FlatFileItemReader<String> s3FlatFileItemReader,
            ItemProcessor<String, Object> patientItemProcessor,
            ItemWriter<Object> patientErrorWriter) {

        return new StepBuilder("patientStep", jobRepository)
                .<String, Object>chunk(5000, transactionManager) // tuned chunk size
                .reader(s3FlatFileItemReader)
                .processor(patientItemProcessor)
                .writer(patientErrorWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(Integer.MAX_VALUE)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> s3FlatFileItemReader(@Value("#{jobParameters['fileKey']}") String fileKey) {
        return s3BatchReader.s3FlatFileItemReader(fileKey);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Adjust based on CPU
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
