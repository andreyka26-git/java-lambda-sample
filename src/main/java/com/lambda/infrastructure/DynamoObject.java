package com.lambda.infrastructure;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;

public class DynamoObject {

    @DynamoDBHashKey(attributeName="Id")
    private int id;

    @DynamoDBAttribute(attributeName="Count")
    private int count;

    @DynamoDBAttribute(attributeName="Json")
    private String json;

    @DynamoDBAttribute(attributeName="Quantity")
    private int quantity;

    public DynamoObject(int id, int count, String json, int quantity) {
        this.id = id;
        this.count = count;
        this.json = json;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public String getJson() {
        return json;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
