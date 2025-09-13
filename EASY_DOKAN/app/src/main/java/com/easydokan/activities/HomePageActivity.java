package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easydokan.R;
import com.easydokan.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomePageActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference productsRef;
    private CollectionReference salesRef;
    private CollectionReference customersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setupUserInfo();
        setupDateTime();
        setupNavigation();
        fetchQuickStats();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            productsRef = db.collection("users").document(userId).collection("products");
            salesRef = db.collection("users").document(userId).collection("sales");
            customersRef = db.collection("users").document(userId).collection("customers");
        } else {
            // This should not happen if the user is on this page, but as a safeguard:
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                binding.welcomeText.setText(getString(R.string.welcome_user, displayName));
            } else if (currentUser.getEmail() != null) {
                binding.welcomeText.setText(getString(R.string.welcome_user, currentUser.getEmail()));
            } else {
                binding.welcomeText.setText(getString(R.string.welcome_user, "User"));
            }
        }
    }

    private void setupDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        binding.dateTimeText.setText(currentDate);
    }

    private void setupNavigation() {
        binding.cardDashboard.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        binding.cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesActivity.class)));
        binding.cardProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductActivity.class)));
        binding.cardCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        binding.cardReceiveDsr.setOnClickListener(v -> startActivity(new Intent(this, ReceiveFromDsrActivity.class)));
        binding.settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        binding.aboutButton.setOnClickListener(v -> {
            // A simple Toast for the about button as requested by the review.
            Toast.makeText(this, "Easy Dokan v1.0", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchQuickStats() {
        if (mAuth.getCurrentUser() == null) return;

        // Today's Sales
        fetchTodaysSales();
        // Total Products
        fetchTotalProducts();
        // Low Stock
        fetchLowStockAlerts();
        // Pending Payments
        fetchPendingDues();
    }

    private void fetchTodaysSales() {
        if (salesRef == null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        salesRef.whereGreaterThanOrEqualTo("createdAt", startOfDay).get().addOnSuccessListener(snapshots -> {
            double todaysSales = 0;
            for (QueryDocumentSnapshot doc : snapshots) {
                if (doc.contains("total")) {
                    todaysSales += doc.getDouble("total");
                }
            }
            binding.statTodaysSales.setText("Today’s Sales: " + formatCurrency(todaysSales));
        });
    }

    private void fetchTotalProducts() {
        if (productsRef == null) return;
        productsRef.get().addOnSuccessListener(snapshots -> {
            binding.statTotalProducts.setText("Total Products: " + snapshots.size());
        });
    }

    private void fetchLowStockAlerts() {
        if (productsRef == null) return;
        productsRef.whereLessThanOrEqualTo("stock", 10).get().addOnSuccessListener(snapshots -> {
            binding.statLowStock.setText("Low Stock Items: " + snapshots.size());
        });
    }

    private void fetchPendingDues() {
        if (customersRef == null) return;
        customersRef.whereGreaterThan("due", 0).get().addOnSuccessListener(snapshots -> {
            double totalDues = 0;
            for (QueryDocumentSnapshot doc : snapshots) {
                if (doc.contains("due")) {
                    totalDues += doc.getDouble("due");
                }
            }
            binding.statPendingPayments.setText("Pending Payments: " + formatCurrency(totalDues));
        });
    }

    private String formatCurrency(double amount) {
        try {
            return NumberFormat.getCurrencyInstance(new Locale("bn", "BD")).format(amount);
        } catch (Exception e) {
            return NumberFormat.getCurrencyInstance().format(amount);
        }
    }
}
