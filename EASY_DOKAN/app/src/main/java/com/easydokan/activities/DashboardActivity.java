package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.easydokan.R;
import com.easydokan.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference productsRef;
    private CollectionReference salesRef;
    private CollectionReference customersRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }


        initFirebase();
        fetchDashboardData();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            productsRef = db.collection("users").document(userId).collection("products");
            salesRef = db.collection("users").document(userId).collection("sales");
            customersRef = db.collection("users").document(userId).collection("customers");
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to LoginActivity
            finish();
        }
    }

    private void fetchDashboardData() {
        if (mAuth.getCurrentUser() == null) return;

        fetchTotalProducts();
        fetchTotalSales();
        fetchTodaysSales();
        fetchPendingDues();
        fetchLowStockAlerts();
    }

    private void fetchTotalProducts() {
        if (productsRef == null) return;
        productsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalProducts = queryDocumentSnapshots.size();
            binding.totalProductsText.setText(getString(R.string.dashboard_total_products, totalProducts));
        }).addOnFailureListener(e -> {
            binding.totalProductsText.setText(getString(R.string.dashboard_total_products_error));
        });
    }

    private void fetchTotalSales() {
        if (salesRef == null) return;
        salesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            double totalSales = 0;
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                if (document.contains("total")) {
                    totalSales += document.getDouble("total");
                }
            }
            binding.totalSalesText.setText(getString(R.string.dashboard_total_sales, formatCurrency(totalSales)));
        }).addOnFailureListener(e -> {
            binding.totalSalesText.setText(getString(R.string.dashboard_total_sales_error));
        });
    }

    private void fetchTodaysSales() {
        if (salesRef == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        salesRef.whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                double todaysSales = 0;
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.contains("total")) {
                        todaysSales += document.getDouble("total");
                    }
                }
                binding.todaysSalesText.setText(getString(R.string.dashboard_todays_sales, formatCurrency(todaysSales)));
            }).addOnFailureListener(e -> {
                binding.todaysSalesText.setText(getString(R.string.dashboard_todays_sales_error));
            });
    }

    private void fetchPendingDues() {
        if (customersRef == null) return;
        customersRef.whereGreaterThan("due", 0)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                double totalDues = 0;
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.contains("due")) {
                        totalDues += document.getDouble("due");
                    }
                }
                binding.pendingDuesText.setText(getString(R.string.dashboard_pending_dues, formatCurrency(totalDues)));
            }).addOnFailureListener(e -> {
                binding.pendingDuesText.setText(getString(R.string.dashboard_pending_dues_error));
            });
    }

    private void fetchLowStockAlerts() {
        if (productsRef == null) return;
        final int LOW_STOCK_THRESHOLD = 10;
        productsRef.whereLessThanOrEqualTo("stock", LOW_STOCK_THRESHOLD)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int lowStockItems = queryDocumentSnapshots.size();
                binding.lowStockAlertsText.setText(getString(R.string.dashboard_low_stock_alerts, lowStockItems));
            }).addOnFailureListener(e -> {
                binding.lowStockAlertsText.setText(getString(R.string.dashboard_low_stock_alerts_error));
            });
    }

    private String formatCurrency(double amount) {
        // Using Locale("bn", "BD") for Bengali, Bangladesh to get the ৳ symbol.
        // Fallback to default locale if it fails.
        try {
            return NumberFormat.getCurrencyInstance(new Locale("bn", "BD")).format(amount);
        } catch (Exception e) {
            return NumberFormat.getCurrencyInstance().format(amount);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
