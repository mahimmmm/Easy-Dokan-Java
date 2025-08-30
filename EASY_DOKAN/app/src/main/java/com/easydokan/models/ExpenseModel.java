package com.easydokan.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ExpenseModel {

    @Exclude
    private String id;
    private String title;
    private double amount;
    private String category;
    @ServerTimestamp
    private Date date;

    // Required empty public constructor for Firestore
    public ExpenseModel() {
    }

    public ExpenseModel(String title, double amount, String category) {
        this.title = title;
        this.amount = amount;
        this.category = category;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
