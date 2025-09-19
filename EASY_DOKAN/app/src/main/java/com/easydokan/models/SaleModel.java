package com.easydokan.models;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class SaleModel {

    private DocumentReference customer_id;
    private List<SaleItem> items;
    private double total_amount;
    private double paid_amount;
    private double due_amount;
    private String payment_method;
    @ServerTimestamp
    private Date created_at;

    public SaleModel() {
        // Required empty public constructor for Firestore
    }

    // Getters and Setters
    public DocumentReference getCustomer_id() { return customer_id; }
    public void setCustomer_id(DocumentReference customer_id) { this.customer_id = customer_id; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
    public double getTotal_amount() { return total_amount; }
    public void setTotal_amount(double total_amount) { this.total_amount = total_amount; }
    public double getPaid_amount() { return paid_amount; }
    public void setPaid_amount(double paid_amount) { this.paid_amount = paid_amount; }
    public double getDue_amount() { return due_amount; }
    public void setDue_amount(double due_amount) { this.due_amount = due_amount; }
    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
}
