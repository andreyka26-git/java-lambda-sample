package com.lambda.infrastructure;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "")
public class DynamoObject {
    private int id;
    private int count;
    private String json;
    private int quantity;

    public DynamoObject(int id, int count, String json, int quantity) {
        this.id = id;
        this.count = count;
        this.json = json;
        this.quantity = quantity;
    }

    public DynamoObject() {
        // used for dynamo mapper
    }

    @DynamoDBHashKey(attributeName="Id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName="Count")
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @DynamoDBAttribute(attributeName="Json")
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @DynamoDBAttribute(attributeName="Quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
