package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
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

        // We don't use setSupportActionBar anymore to have full control.
        // The title is already set in the XML.

        // New direct method to inflate menu and set listener
        binding.toolbar.inflateMenu(R.menu.toolbar_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                // We need a view to anchor the popup to. The toolbar itself is a stable anchor.
                showProfileMenu(binding.toolbar);
                return true;
            }
            return false;
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.cardCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        binding.cardProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductActivity.class)));
        binding.cardSales.setOnClickListener(v -> startActivity(new Intent(this, SalesActivity.class)));
        binding.cardExpenses.setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));
    }

    private void showProfileMenu(View anchor) {
        // Use Gravity.END to align the popup to the right side of the toolbar.
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
