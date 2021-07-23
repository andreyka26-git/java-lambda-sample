package com.lambda.config;

import com.amazonaws.services.s3.model.S3DataSource;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {


    @JsonProperty("s3")
    private S3Config s3;

    @JsonProperty("dynamo_db")
    private DynamoDbConfig dynamoDb;

    @JsonProperty("region")
    private String region;

    public S3Config getS3() {
        return s3;
    }

    public DynamoDbConfig getDynamoDb() {
        return dynamoDb;
    }

    public String getRegion() {
        return region;
    }

    public void setS3(S3Config s3) {
        this.s3 = s3;
    }

    public void setDynamoDb(DynamoDbConfig dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
