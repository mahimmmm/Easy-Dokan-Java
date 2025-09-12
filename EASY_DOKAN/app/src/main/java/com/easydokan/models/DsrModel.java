package com.easydokan.models;

import com.google.firebase.firestore.Exclude;

public class DsrModel {
    @Exclude
    private String id;
    private String name;
    private String phone;
    private String company;
    private String address;

    public DsrModel() {
        // Required empty public constructor for Firestore
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return name; // For display in AutoCompleteTextView
    }
}
