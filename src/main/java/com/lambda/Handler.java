package com.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.config.Config;
import com.lambda.domain.DynamoObject;
import com.lambda.domain.SampleObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Handler {
    private final static Config config = Config.loadFromFile("config.json");

    private final static int id = 1;
    private final static ObjectMapper jsonMapper = new ObjectMapper();

    private final static AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(config.getRegion()).build();

    private final static AmazonDynamoDB dynamoClient = AmazonDynamoDBClientBuilder.standard()
            //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Consts.AWS_REGION))
            .withRegion(config.getRegion())
            .build();

    public void handleRequest(Map<String,String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            ArrayList<SampleObject> objects;
            String filePath = event.get("s3Path");

            try {
                objects = getSampleObjects(config.getS3().getBucket(), filePath);
            } catch (IOException e) {
                logger.log("Error occurred when try to get json from s3. Error: " + e);
                return;
            }

            Integer countAggregation = objects.stream().map(o -> o.getCount())
                    .reduce(0, Integer::sum);

            Integer quantityAggregation = objects.stream().map(o -> o.getQuantity())
                    .reduce(0, Integer::sum);

            String json = "";
            try{
                json = jsonMapper.writeValueAsString(objects);
            }
            catch(JsonProcessingException e) {
                logger.log("Error occurred when try to deserialize objects json. " + e);
                return;
            }

            DynamoObject obj = new DynamoObject(id, countAggregation, json, quantityAggregation);
            dropIdExists(id);
            addToDynamo(obj);

            logger.log("Lambda finished correctly.");
        }
        catch(Exception e) {
            logger.log("Global error in lambda occurred " + e);
        }
    }

    private static void dropIdExists(int id) {
        HashMap<String,AttributeValue> idValues = new HashMap<String,AttributeValue>();

        idValues.put("Id", new AttributeValue().withN(String.valueOf(id)));

        GetItemRequest request = new GetItemRequest()
                .withKey(idValues)
                .withTableName(config.getDynamoDb().getTable());

        Map<String,AttributeValue> item =
                dynamoClient.getItem(request).getItem();

        if (item != null) {
            DeleteItemRequest deleteRequest = new DeleteItemRequest()
                    .withTableName(config.getDynamoDb().getTable())
                    .withKey(idValues);

            dynamoClient.deleteItem(deleteRequest);
        }
    }

    private static void addToDynamo(DynamoObject obj) {
        HashMap<String,AttributeValue> values = new HashMap<String,AttributeValue>();

        values.put("Id", new AttributeValue().withN(String.valueOf(obj.getId())));
        values.put("Count", new AttributeValue(String.valueOf(obj.getCount())));
        values.put("Json", new AttributeValue(String.valueOf(obj.getJson())));
        values.put("Quantity", new AttributeValue(String.valueOf(obj.getQuantity())));

        dynamoClient.putItem(config.getDynamoDb().getTable(), values);
    }

    private static ArrayList<SampleObject> getSampleObjects(String bucketName, String path) throws IOException {
        Bucket bucket  = s3Client.listBuckets().stream()
                .filter(b -> b.getName().equals(bucketName))
                .findFirst()
                .get();

        ListObjectsV2Result filesResult = s3Client.listObjectsV2(bucket.getName());
        Optional<S3ObjectSummary> file = filesResult.getObjectSummaries().stream()
                .filter(f -> f.getKey().equals(path))
                .findFirst();

        if (!file.isPresent())
            throw new IOException("Cannot find file with path " + path);

        S3Object fileObject = s3Client.getObject(bucket.getName(), file.get().getKey());

        try(S3ObjectInputStream sampleObjectsStream = fileObject.getObjectContent()){
            String json = IOUtils.toString(sampleObjectsStream);

            TypeReference<ArrayList<SampleObject> > typeRef = new TypeReference<ArrayList<SampleObject> >(){};
            ArrayList<SampleObject> objects = jsonMapper.readValue(json, typeRef);

            return objects;
        } catch (IOException e) {
            throw e;
        }
    }
}
