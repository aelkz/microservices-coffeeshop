package com.redhat.microservices.coffeeshop.order.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Storage {

    private String id;
    private Double totalMilk; // grams
    private Double totalCoffee; // grams
    private Double milk; // grams
    private Double coffee; // grams
    private String transaction;
    private Storage.Operation op;

    public static enum Operation {
        IN, OUT;
    }

    public Double getTotalMilk() {
        return totalMilk;
    }

    public void setTotalMilk(Double totalMilk) {
        this.totalMilk = totalMilk;
    }

    public Double getTotalCoffee() {
        return totalCoffee;
    }

    public void setTotalCoffee(Double totalCoffee) {
        this.totalCoffee = totalCoffee;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
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

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
