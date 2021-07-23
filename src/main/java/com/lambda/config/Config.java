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
    public static Config loadFromFile(String resourceFileName) {
        Logger logger = LoggerFactory.getLogger(Config.class);

        logger.info("Started read config");
        ClassLoader classLoader = S3DataSource.Utils.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(resourceFileName)) {
            String json = IOUtils.toString(is);
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Config> typeRef = new TypeReference<Config>(){};

            Config config = mapper.readValue(json, typeRef);
            return config;
        } catch (Exception ex) {
            logger.error("Cannot read config." + ex);
            return null;
        }
    }

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
}
