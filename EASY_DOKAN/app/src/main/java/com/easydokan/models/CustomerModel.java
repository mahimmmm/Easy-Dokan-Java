package com.easydokan.models;

import com.google.firebase.firestore.Exclude;

public class CustomerModel {

    @Exclude
    private String id;
    private String name;
    private String phone;
    private String address;
    private double openingBalance;
    private double due;
    private String notes;

    // Required empty public constructor for Firestore
    public CustomerModel() {
    }

    // Getters and Setters
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

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public double getDue() {
        return due;
    }

    public void setDue(double due) {
        this.due = due;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return name;
    }
}
