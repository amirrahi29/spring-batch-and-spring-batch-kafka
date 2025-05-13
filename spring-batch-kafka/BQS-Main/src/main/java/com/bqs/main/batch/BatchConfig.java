package com.bqs.main.batch;

import com.bqs.main.batch.listener.S3JobExecutionListener;
import com.bqs.main.model.PatientError;
import com.bqs.main.model.PatientWrapper;
import com.bqs.main.repositories.ErrorRepository;
import com.bqs.main.repositories.PatientRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private com.bqs.main.batch.reader.S3BatchReader s3BatchReader;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ErrorRepository patientErrorRepository;

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
            ItemProcessor<String, PatientWrapper> patientItemProcessor,
            ItemWriter<PatientWrapper> classifierItemWriter) {

        return new StepBuilder("patientStep", jobRepository)
                .<String, PatientWrapper>chunk(5000, transactionManager)
                .reader(s3FlatFileItemReader)
                .processor(patientItemProcessor)
                .writer(classifierItemWriter)
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
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    public ItemWriter<PatientWrapper> classifierItemWriter() {
        ClassifierCompositeItemWriter<PatientWrapper> compositeWriter = new ClassifierCompositeItemWriter<>();
        compositeWriter.setClassifier((Classifier<PatientWrapper, ItemWriter<? super PatientWrapper>>) wrapper -> {
            if (wrapper.isValid()) {
                return validPatientWriter();
            } else {
                return invalidPatientWriter();
            }
        });
        return compositeWriter;
    }

    @Bean
    public ItemWriter<PatientWrapper> validPatientWriter() {
        return wrappers -> {
            for (PatientWrapper wrapper : wrappers) {
                patientRepository.save(wrapper.getPatient());
            }
        };
    }

    @Bean
    public ItemWriter<PatientWrapper> invalidPatientWriter() {
        return wrappers -> {
            for (PatientWrapper wrapper : wrappers) {
                PatientError error = new PatientError();
                error.setRawLine(wrapper.getRawLine());
                error.setErrorMessage(wrapper.getErrorMessage());
                patientErrorRepository.save(error);
            }
        };
    }
}
