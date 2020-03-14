package com.redhat.microservices.coffeeshop.order.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    private String id;
    private String name;
    private Double milk; // grams
    private Double coffee; // grams

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMilk() {
        return milk;
    }

    public void setMilk(Double milk) {
        this.milk = milk;
    }

    public Double getCoffee() {
        return coffee;
    }

    public void setCoffee(Double coffee) {
        this.coffee = coffee;
    }

    public Product() { }

    public Product(String id, String name, Double milk, Double coffee) {
        this.id = id;
        this.name = name;
        this.milk = milk;
        this.coffee = coffee;
    }
}
