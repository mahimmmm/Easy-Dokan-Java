package com.easydokan.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role; // "Shop Owner" or "Staff"
    @ServerTimestamp
    private Date createdAt;

    // Required empty public constructor for Firestore
    public UserModel() {
    }

    public UserModel(String uid, String name, String email, String phone, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
