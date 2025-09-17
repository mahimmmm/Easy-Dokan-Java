package com.easydokan.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class CustomerModel {

    @Exclude
    private String id;
    private String name;
    private String phone;
    private String address;
    private double total_due;
    @ServerTimestamp
    private Date created_at;
    @ServerTimestamp
    private Date updated_at;

    public CustomerModel() {
        // Required empty public constructor for Firestore
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getTotal_due() { return total_due; }
    public void setTotal_due(double total_due) { this.total_due = total_due; }
    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }

    @Override
    public String toString() {
        return name;
    }
}
