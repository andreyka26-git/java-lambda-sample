package com.lambda.domain;
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
}
