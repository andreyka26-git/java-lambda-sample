package com.lambda.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class S3Config {
    @JsonProperty("bucket")
    private String bucket;

    public String getBucket() {
        return bucket;
    }
}
