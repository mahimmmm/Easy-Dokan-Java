package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.easydokan.R;
import com.easydokan.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(binding.toolbar);

        setupClickListeners();
        // TODO: Load summary data from Firebase
    }

    private void setupClickListeners() {
        binding.cardCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        binding.cardProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductActivity.class)));
        binding.cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesActivity.class)));
        binding.cardExpenses.setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            showProfileMenu(binding.toolbar);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_view_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.action_reports) {
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            } else if (itemId == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                logoutUser();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
