package com.bqs.main.batch.reader;

import com.bqs.main.s3.S3InputStreamFetcher;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.batch.core.configuration.annotation.StepScope;

@Configuration
public class S3BatchReader {

    @Autowired
    private S3InputStreamFetcher s3InputStreamFetcher;

    @StepScope
    @Bean
    public FlatFileItemReader<String> s3FlatFileItemReader(@Value("#{jobParameters['fileKey']}") String fileKey) {
        InputStreamResource resource = s3InputStreamFetcher.fetch(fileKey);

        return new FlatFileItemReaderBuilder<String>()
                .name("s3-reader-" + fileKey)
                .resource(resource)
                .linesToSkip(1) // skip header if needed
                .lineMapper(new PassThroughLineMapper())
                .build();
    }
}

