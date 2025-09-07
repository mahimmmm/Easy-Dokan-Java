package com.easydokan.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
            setupRecyclerView(customerRef.orderBy("name", Query.Direction.ASCENDING));
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddCustomer.setOnClickListener(v -> showAddEditCustomerDialog(null));
    }

    private void setupRecyclerView(Query query) {
        FirestoreRecyclerOptions<CustomerModel> options = new FirestoreRecyclerOptions.Builder<CustomerModel>()
                .setQuery(query, CustomerModel.class)
                .build();

        adapter = new CustomerAdapter(options);
        binding.customerRecyclerView.setHasFixedSize(true);
        binding.customerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.customerRecyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot) {
                // TODO: Navigate to Ledger Activity
                Toast.makeText(CustomerActivity.this, "Ledger view coming soon!", Toast.LENGTH_SHORT).show();
            }
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_customer, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        final EditText phoneEt = dialogView.findViewById(R.id.phone_edit_text);
        final EditText addressEt = dialogView.findViewById(R.id.address_edit_text);
        final EditText balanceEt = dialogView.findViewById(R.id.opening_balance_edit_text);
        final EditText notesEt = dialogView.findViewById(R.id.notes_edit_text);

        if (snapshot != null) {
            builder.setTitle(R.string.edit_customer);
            CustomerModel customer = snapshot.toObject(CustomerModel.class);
            nameEt.setText(customer.getName());
            phoneEt.setText(customer.getPhone());
            addressEt.setText(customer.getAddress());
            balanceEt.setText(String.valueOf(customer.getOpeningBalance()));
            notesEt.setText(customer.getNotes());
        } else {
            builder.setTitle(R.string.add_new_customer);
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomerModel customer = new CustomerModel();
            customer.setName(name);
            customer.setPhone(phone);
            customer.setAddress(addressEt.getText().toString().trim());
            customer.setNotes(notesEt.getText().toString().trim());
            try {
                customer.setOpeningBalance(Double.parseDouble(balanceEt.getText().toString()));
            } catch (NumberFormatException e) {
                customer.setOpeningBalance(0.0);
            }

            if (snapshot != null) {
                customerRef.document(snapshot.getId()).set(customer);
                Toast.makeText(this, "Customer updated", Toast.LENGTH_SHORT).show();
            } else {
                customerRef.add(customer);
                Toast.makeText(this, "Customer added", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint(getString(R.string.search_customers_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        return true;
    }

    private void performSearch(String text) {
        Query query;
        if (text.isEmpty()) {
            query = customerRef.orderBy("name", Query.Direction.ASCENDING);
        } else {
            // This search is case-sensitive and only matches prefixes.
            // For a more robust search, Cloud Functions or a third-party service like Algolia is needed.
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
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
