package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.easydokan.R;
import com.easydokan.adapters.DashboardAdapter;
import com.easydokan.databinding.ActivityDashboardBinding;
import com.easydokan.models.DashboardNavItem;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setupNavigationRecyclerView();
    }

    private void setupNavigationRecyclerView() {
        binding.navRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<DashboardNavItem> navItems = new ArrayList<>();
        navItems.add(new DashboardNavItem(getString(R.string.customers), R.drawable.ic_customer));
        navItems.add(new DashboardNavItem(getString(R.string.products), R.drawable.ic_products));
        navItems.add(new DashboardNavItem(getString(R.string.sales), R.drawable.ic_sales));
        navItems.add(new DashboardNavItem(getString(R.string.expenses), R.drawable.ic_expense));
        navItems.add(new DashboardNavItem(getString(R.string.reports), R.drawable.ic_reports));

        adapter = new DashboardAdapter(navItems);
        binding.navRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            String title = item.getTitle();
            if (title.equals(getString(R.string.customers))) {
                startActivity(new Intent(this, CustomerActivity.class));
            } else if (title.equals(getString(R.string.products))) {
                startActivity(new Intent(this, ProductActivity.class));
            } else if (title.equals(getString(R.string.sales))) {
                startActivity(new Intent(this, SalesActivity.class));
            } else if (title.equals(getString(R.string.expenses))) {
                startActivity(new Intent(this, ExpenseActivity.class));
            } else if (title.equals(getString(R.string.reports))) {
                startActivity(new Intent(this, ReportActivity.class));
            }
        });
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
