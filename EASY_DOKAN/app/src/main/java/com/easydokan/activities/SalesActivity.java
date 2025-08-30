package com.easydokan.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.easydokan.adapters.SalesAdapter;
import com.easydokan.databinding.ActivitySalesBinding;
import com.easydokan.models.SaleModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SalesActivity extends AppCompatActivity {

    private ActivitySalesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SalesAdapter adapter;
    private CollectionReference salesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            salesRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("sales");
            setupRecyclerView();
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddSale.setOnClickListener(v -> {
            startActivity(new Intent(this, NewSaleActivity.class));
        });
    }

    private void setupRecyclerView() {
        Query query = salesRef.orderBy("saleDate", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<SaleModel> options = new FirestoreRecyclerOptions.Builder<SaleModel>()
                .setQuery(query, SaleModel.class)
                .build();

        adapter = new SalesAdapter(options);
        binding.salesRecyclerView.setHasFixedSize(true);
        binding.salesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.salesRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(documentSnapshot -> {
            // TODO: Open Sale Detail Activity
            Toast.makeText(this, "Sale detail view coming soon!", Toast.LENGTH_SHORT).show();
        });
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
