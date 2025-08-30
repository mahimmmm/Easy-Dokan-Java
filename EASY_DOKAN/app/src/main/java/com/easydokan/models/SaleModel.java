package com.easydokan.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class SaleModel {

    @Exclude
    private String id;
    private String customerId;
    private String customerName;
    private double totalAmount;
    private String paymentMode; // e.g., "Cash", "Card"
    @ServerTimestamp
    private Date saleDate;

    // This will not be stored in the Sale document itself, but fetched from the subcollection.
    @Exclude
    private List<SaleItemModel> saleItems;

    public SaleModel() {
    }

    public SaleModel(String customerId, String customerName, double totalAmount, String paymentMode) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }

    @Exclude
    public List<SaleItemModel> getSaleItems() {
        return saleItems;
    }

    @Exclude
    public void setSaleItems(List<SaleItemModel> saleItems) {
        this.saleItems = saleItems;
    }
}
