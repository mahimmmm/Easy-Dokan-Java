package com.easydokan.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.easydokan.R;
import com.easydokan.models.CustomerModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SalesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AutoCompleteTextView customerAutocomplete;
    private AutoCompleteTextView productAutocomplete;
    private android.widget.TextView previousDueTextView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference customerRef;
    private CollectionReference productRef;

    private List<CustomerModel> customerList;
    private ArrayAdapter<CustomerModel> customerAdapter;
    private List<ProductModel> productList;
    private ArrayAdapter<ProductModel> productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Sale");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initFirebase();
        initViews();

        loadCustomers();
        loadProducts();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            customerRef = db.collection("users").document(userId).collection("customers");
            productRef = db.collection("users").document(userId).collection("products");
        } else {
            // Handle user not logged in
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        customerAutocomplete = findViewById(R.id.customer_autocomplete);
        previousDueTextView = findViewById(R.id.previous_due_textview);
        customerList = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerList);
        customerAutocomplete.setAdapter(customerAdapter);

        customerAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            CustomerModel selectedCustomer = (CustomerModel) parent.getItemAtPosition(position);
            if (selectedCustomer != null) {
                double due = selectedCustomer.getDue();
                if (due > 0) {
                    previousDueTextView.setText("Previous Due: ৳ " + due);
                    previousDueTextView.setVisibility(android.view.View.VISIBLE);
                } else {
                    previousDueTextView.setVisibility(android.view.View.GONE);
                }
            }
        });

        productAutocomplete = findViewById(R.id.product_autocomplete);
        productList = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productList);
        productAutocomplete.setAdapter(productAdapter);
    }

    private void loadCustomers() {
        if (customerRef == null) return;

        customerRef.orderBy("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                customerList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CustomerModel customer = document.toObject(CustomerModel.class);
                    customer.setId(document.getId());
                    customerList.add(customer);
                }
                customerAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(SalesActivity.this, "Failed to load customers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        if (productRef == null) return;

        productRef.orderBy("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    ProductModel product = document.toObject(ProductModel.class);
                    product.setId(document.getId());
                    productList.add(product);
                }
                productAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(SalesActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
