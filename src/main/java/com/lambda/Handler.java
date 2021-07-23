package com.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.config.Config;
import com.lambda.config.ConfigFactory;
import com.lambda.infrastructure.DynamoObject;
import com.lambda.domain.SampleObject;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Handler implements RequestHandler<Map<String, String>, String> {
    private final String ERROR_RESPONSE = "ERROR";
    private final String OK_RESPONSE = "OK";
    private final String FILEPATH_TO_S3_KEY = "s3Path";
    private final int ID = 1;

    private final Config config;
    private final ObjectMapper jsonMapper;
    private final AmazonS3 s3Client;
    private final AmazonDynamoDB dynamoClient;
    private final DynamoDBMapper dynamoMapper;

    public Handler() {
        config = ConfigFactory.loadFromFile("config.json");

        jsonMapper = new ObjectMapper();

        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(config.getRegion())
                .build();

        dynamoClient = AmazonDynamoDBClientBuilder.standard()
                //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Consts.AWS_REGION))
                .withRegion(config.getRegion())
                .build();

        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            String filePath = event.get(FILEPATH_TO_S3_KEY);

            if (StringUtils.equals(filePath, ""))
                throw new Exception("Cannot get file path from request by" + FILEPATH_TO_S3_KEY + " key.");

            ArrayList<SampleObject> objects;
            try {
                objects = getSampleObjects(config.getS3().getBucket(), filePath);
            } catch (IOException e) {
                logger.log("Error occurred when try to get json from s3. Error: " + e);
                throw e;
            }

            int countAggregation = objects.stream().map(o -> o.getCount())
                    .reduce(0, Integer::sum);

            int quantityAggregation = objects.stream().map(o -> o.getQuantity())
                    .reduce(0, Integer::sum);

            String json = "";
            try{
                json = jsonMapper.writeValueAsString(objects);
            }
            catch(JsonProcessingException e) {
                logger.log("Error occurred when try to deserialize objects json. " + e);
                throw e;
            }

            DynamoObject obj = new DynamoObject(ID, countAggregation, json, quantityAggregation);
            dropIdExists(ID);
            addToDynamo(obj);

            logger.log("Lambda finished correctly.");
        }
        catch(Exception e) {
            logger.log("Global error in lambda occurred " + e);
            return ERROR_RESPONSE;
        }

        return OK_RESPONSE;
    }

    private void dropIdExists(int id) {
        DynamoObject object = dynamoMapper.load(DynamoObject.class, ID);

        if (object != null) {
            dynamoMapper.delete(object);
        }
    }

    private void addToDynamo(DynamoObject obj) {
        dynamoMapper.save(obj);
    }

    private ArrayList<SampleObject> getSampleObjects(String bucketName, String path) throws IOException {
        Optional<Bucket> bucket  = s3Client.listBuckets().stream()
                .filter(b -> StringUtils.equals(b.getName(), bucketName))
                .findFirst();

        if (!bucket.isPresent())
            throw new IOException("Cannot find bucket with bucket name " + bucketName);

        ListObjectsV2Result filesResult = s3Client.listObjectsV2(bucket.get().getName());

        Optional<S3ObjectSummary> file = filesResult.getObjectSummaries().stream()
                .filter(f -> StringUtils.equals(f.getKey(), path))
                .findFirst();

        if (!file.isPresent())
            throw new IOException("Cannot find file with path " + path);

        S3Object fileObject = s3Client.getObject(bucket.get().getName(), file.get().getKey());

        try(S3ObjectInputStream sampleObjectsStream = fileObject.getObjectContent()){
            String json = IOUtils.toString(sampleObjectsStream, StandardCharsets.UTF_8);

            TypeReference<ArrayList<SampleObject> > typeRef = new TypeReference<ArrayList<SampleObject> >(){};
            ArrayList<SampleObject> objects = jsonMapper.readValue(json, typeRef);

            return objects;
        } catch (IOException e) {
            throw e;
        }
    }
}
