package com.easydokan.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.easydokan.R;
import com.easydokan.adapters.CustomerAdapter;
import com.easydokan.databinding.ActivityCustomerBinding;
import com.easydokan.models.CustomerModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CustomerActivity extends AppCompatActivity {

    private ActivityCustomerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CustomerAdapter adapter;
    private CollectionReference customerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            customerRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("customers");
            setupRecyclerView();
        } else {
            // Handle user not logged in case
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddCustomer.setOnClickListener(v -> showAddEditCustomerDialog(null));

        setupSearch();
    }

    private void setupRecyclerView() {
        Query query = customerRef.orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<CustomerModel> options = new FirestoreRecyclerOptions.Builder<CustomerModel>()
                .setQuery(query, CustomerModel.class)
                .build();

        adapter = new CustomerAdapter(options);
        binding.customerRecyclerView.setHasFixedSize(true);
        binding.customerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.customerRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(DocumentSnapshot documentSnapshot) {
                showAddEditCustomerDialog(documentSnapshot);
            }

            @Override
            public void onDeleteClick(DocumentSnapshot documentSnapshot) {
                new AlertDialog.Builder(CustomerActivity.this)
                        .setTitle("Delete Customer")
                        .setMessage("Are you sure you want to delete this customer?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            documentSnapshot.getReference().delete();
                            Toast.makeText(CustomerActivity.this, "Customer deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void showAddEditCustomerDialog(DocumentSnapshot snapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_customer, null);
        builder.setView(dialogView);

        final EditText nameEditText = dialogView.findViewById(R.id.name_edit_text);
        final EditText phoneEditText = dialogView.findViewById(R.id.phone_edit_text);
        final EditText addressEditText = dialogView.findViewById(R.id.address_edit_text);

        final String customerId = (snapshot != null) ? snapshot.getId() : null;
        if (snapshot != null) {
            CustomerModel customer = snapshot.toObject(CustomerModel.class);
            nameEditText.setText(customer.getName());
            phoneEditText.setText(customer.getPhone());
            addressEditText.setText(customer.getAddress());
            builder.setTitle("Edit Customer");
        } else {
            builder.setTitle("Add Customer");
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomerModel customer = new CustomerModel(name, phone, address);
            if (customerId != null) {
                // Update
                customerRef.document(customerId).set(customer);
                Toast.makeText(this, "Customer updated", Toast.LENGTH_SHORT).show();
            } else {
                // Create
                customerRef.add(customer);
                Toast.makeText(this, "Customer added", Toast.length_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
    }

    private void performSearch(String text) {
        Query query;
        if (text.isEmpty()) {
            query = customerRef.orderBy("name", Query.Direction.ASCENDING);
        } else {
            query = customerRef.orderBy("name").startAt(text).endAt(text + "\uf8ff");
        }

        FirestoreRecyclerOptions<CustomerModel> options = new FirestoreRecyclerOptions.Builder<CustomerModel>()
                .setQuery(query, CustomerModel.class)
                .build();

        adapter.updateOptions(options);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
