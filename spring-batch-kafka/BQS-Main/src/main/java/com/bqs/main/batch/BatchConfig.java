package com.bqs.main.batch;

import com.bqs.main.batch.listener.S3JobExecutionListener;
import com.bqs.main.batch.reader.S3BatchReader;
import com.bqs.main.config.kafka.KafkaPatientWriter;
import com.bqs.main.model.Patient;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.repositories.PatientRepository;
import com.bqs.main.repositories.ErrorRepository;
import com.bqs.main.validator.PatientValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final S3BatchReader s3BatchReader;
    private final PatientRepository patientRepository;
    private final ErrorRepository patientErrorRepository;
    private final S3JobExecutionListener s3JobExecutionListener;

    public BatchConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager,
                       S3BatchReader s3BatchReader,
                       PatientRepository patientRepository,
                       ErrorRepository patientErrorRepository,
                       S3JobExecutionListener s3JobExecutionListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.s3BatchReader = s3BatchReader;
        this.patientRepository = patientRepository;
        this.patientErrorRepository = patientErrorRepository;
        this.s3JobExecutionListener = s3JobExecutionListener;
    }

    @Bean
    @StepScope
    public PatientItemProcessor processor(@Value("#{jobParameters['fileKey']}") String fileKey,
                                          PatientValidator validator,
                                          PatientRepository patientRepository) {
        return new PatientItemProcessor(fileKey, validator, patientRepository);
    }

    @Bean(name = "s3FlatFileItemReader")
    @StepScope
    public FlatFileItemReader<Patient> s3FlatFileItemReader(@Value("#{jobParameters['fileKey']}") String fileKey) {
        return s3BatchReader.s3FlatFileItemReader(fileKey);
    }

    @Bean
    public Step patientStep(FlatFileItemReader<Patient> s3FlatFileItemReader,
                            ItemProcessor<Patient, PatientWrapper> processor,
                            KafkaPatientWriter kafkaPatientWriter) {
        return new StepBuilder("patientStep", jobRepository)
                .<Patient, PatientWrapper>chunk(5000, transactionManager)
                .reader(s3FlatFileItemReader)
                .processor(processor)
                .writer(kafkaPatientWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(Integer.MAX_VALUE)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job patientJob(Step patientStep) {
        return new JobBuilder("patientJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(patientStep)
                .listener(s3JobExecutionListener)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}