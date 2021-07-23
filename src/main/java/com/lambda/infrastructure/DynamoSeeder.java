package com.lambda.infrastructure;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.lambda.config.Config;

import java.util.ArrayList;
import java.util.List;

public class DynamoSeeder {
    private final static Config config = Config.loadFromFile("config.json");

    public static void main(String[] args) throws InterruptedException {
        setUpDatabase();
    }

    private static void setUpDatabase() throws InterruptedException {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Consts.AWS_REGION))
                .withRegion(config.getRegion())
                .build();

        List<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("Id").withAttributeType("N"));

        List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.HASH));

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(config.getDynamoDb().getTable())
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(5L)
                        .withWriteCapacityUnits(6L));

        CreateTableResult result = client.createTable(request);
        String createdTable = result.getTableDescription().getTableName();
    }
}
