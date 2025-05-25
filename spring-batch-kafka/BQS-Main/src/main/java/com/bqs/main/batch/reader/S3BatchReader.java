package com.bqs.main.batch.reader;

import com.bqs.main.model.Patient;
import com.bqs.main.service.s3.S3InputStreamFetcher;
import com.bqs.main.utility.FileHeaderConstants;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Configuration
public class S3BatchReader {

    @Autowired
    private S3InputStreamFetcher s3InputStreamFetcher;

    @Bean(name = "s3FlatFileItemReaderCustom")
    @StepScope
    public FlatFileItemReader<Patient> s3FlatFileItemReader(@Value("#{jobParameters['fileKey']}") String fileKey) {
        InputStreamResource resource = s3InputStreamFetcher.fetch(fileKey);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String headerLine = reader.readLine();
            List<String> actualHeaders = headerLine != null
                    ? Arrays.asList(headerLine.split(","))
                    : Collections.emptyList();

            // Normalize headers (trim spaces)
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (int i = 0; i < actualHeaders.size(); i++) {
                headerIndexMap.put(actualHeaders.get(i).trim(), i);
            }

            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(",");
            tokenizer.setStrict(false);

            DefaultLineMapper<Patient> lineMapper = new DefaultLineMapper<>();
            lineMapper.setLineTokenizer(tokenizer);
            lineMapper.setFieldSetMapper(new FieldSetMapper<>() {
                @Override
                public Patient mapFieldSet(FieldSet fieldSet) {
                    Patient patient = new Patient();
                    patient.setCorrelationID(getField(fieldSet, headerIndexMap, FileHeaderConstants.CORRELATION_ID));
                    patient.setFirstName(getField(fieldSet, headerIndexMap, FileHeaderConstants.FIRST_NAME));
                    patient.setLastName(getField(fieldSet, headerIndexMap, FileHeaderConstants.LAST_NAME));
                    patient.setAddressLine1(getField(fieldSet, headerIndexMap, FileHeaderConstants.ADDRESS_LINE_1));
                    patient.setAddressLine2(getField(fieldSet, headerIndexMap, FileHeaderConstants.ADDRESS_LINE_2));
                    patient.setCity(getField(fieldSet, headerIndexMap, FileHeaderConstants.CITY));
                    patient.setState(getField(fieldSet, headerIndexMap, FileHeaderConstants.STATE));
                    patient.setZipCode(getField(fieldSet, headerIndexMap, FileHeaderConstants.ZIP_CODE));
                    patient.setEmail(getField(fieldSet, headerIndexMap, FileHeaderConstants.EMAIL_ADDRESS));
                    patient.setMerchantNumber(getField(fieldSet, headerIndexMap, FileHeaderConstants.MERCHANT_NUMBER));
                    patient.setSsn(getField(fieldSet, headerIndexMap, FileHeaderConstants.SSN));
                    patient.setDob(getField(fieldSet, headerIndexMap, FileHeaderConstants.DATE_OF_BIRTH));
                    patient.setPhoneNumber(getField(fieldSet, headerIndexMap, FileHeaderConstants.PHONE_NUMBER));
                    return patient;
                }

                private String getField(FieldSet fieldSet, Map<String, Integer> headerMap, String header) {
                    Integer index = headerMap.get(header);
                    return (index != null && index < fieldSet.getFieldCount()) ? fieldSet.readString(index).trim() : null;
                }
            });

            return new FlatFileItemReaderBuilder<Patient>()
                    .name("s3FlatFileItemReader")
                    .resource(resource)
                    .linesToSkip(1)
                    .lineMapper(lineMapper)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to read header line for file: " + fileKey, e);
        }
    }
}
