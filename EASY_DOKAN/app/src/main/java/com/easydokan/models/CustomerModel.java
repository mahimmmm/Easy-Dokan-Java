package com.easydokan.models;

import com.google.firebase.firestore.Exclude;

public class CustomerModel {

    @Exclude
    private String id;
    private String name;
    private String phone;
    private String address;

    // Required empty public constructor for Firestore deserialization
    public CustomerModel() {
    }

    public CustomerModel(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
