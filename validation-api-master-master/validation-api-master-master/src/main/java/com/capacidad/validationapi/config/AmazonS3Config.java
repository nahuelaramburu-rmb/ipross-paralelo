package com.capacidad.validationapi.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.capacidad.validationapi.misc.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {

    private final ApplicationProperties applicationProperties;

    @Autowired
    public AmazonS3Config(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentials credentials = new BasicAWSCredentials(applicationProperties.getS3Key(),
                applicationProperties.getS3Secret());
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(applicationProperties.getS3Region())
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    @Bean
    public TransferManager transferManager() {
        return TransferManagerBuilder.standard()
                .withS3Client(amazonS3())
                .withMultipartUploadThreshold((long) (5 * 1024 * 1025))
                .build();
    }

}
