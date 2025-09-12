package com.easydokan.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.adapters.DashboardAdapter;
import com.easydokan.models.DashboardNavItem;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView dashboardRecyclerView;
    private DashboardAdapter adapter;
    private List<DashboardNavItem> navItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_dashboard);
        setSupportActionBar(toolbar);

        dashboardRecyclerView = findViewById(R.id.dashboard_recyclerview);
        dashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNavItems();

        adapter = new DashboardAdapter(this, navItems);
        dashboardRecyclerView.setAdapter(adapter);
    }

    private void loadNavItems() {
        navItems = new ArrayList<>();
        // User's new requested navigation list
        navItems.add(new DashboardNavItem("Customers", R.drawable.ic_customer, CustomerActivity.class));
        navItems.add(new DashboardNavItem("Products", R.drawable.ic_product, ProductActivity.class));
        navItems.add(new DashboardNavItem("Sales", R.drawable.ic_sales, SalesActivity.class));
        navItems.add(new DashboardNavItem("Receive From DSR", R.drawable.ic_receive, ReceiveFromDsrActivity.class));
        navItems.add(new DashboardNavItem("Settings", R.drawable.ic_settings, SettingsActivity.class));
    }
}
