package com.easydokan.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ProductModel {

    @Exclude
    private String id;
    private String name;
    private String category;
    private String unit;
    private double price; // Selling price
    private long stock;
    private String added_by;
    @ServerTimestamp
    private Date last_updated;

    public ProductModel() {
        // Required empty public constructor for Firestore
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getStock() { return stock; }
    public void setStock(long stock) { this.stock = stock; }
    public String getAdded_by() { return added_by; }
    public void setAdded_by(String added_by) { this.added_by = added_by; }
    public Date getLast_updated() { return last_updated; }
    public void setLast_updated(Date last_updated) { this.last_updated = last_updated; }

    @Override
    public String toString() {
        return name;
    }
}
