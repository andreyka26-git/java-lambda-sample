package com.lambda.config;

import com.amazonaws.services.s3.model.S3DataSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConfigFactory {
    private static Logger logger = LoggerFactory.getLogger(ConfigFactory.class);

    public static Config loadFromFile(String resourceFileName) {
        ClassLoader classLoader = S3DataSource.Utils.class.getClassLoader();

        try (InputStream is = classLoader.getResourceAsStream(resourceFileName)) {
            String json = IOUtils.toString(is, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            Config config = mapper.readValue(json, Config.class);
            return config;
        } catch (Exception ex) {
            logger.error("Cannot read config." + ex);
            return null;
        }
    }
}
