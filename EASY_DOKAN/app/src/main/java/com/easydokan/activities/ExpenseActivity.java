package com.easydokan.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.easydokan.R;
import com.easydokan.adapters.ExpenseAdapter;
import com.easydokan.databinding.ActivityExpenseBinding;
import com.easydokan.models.ExpenseModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ExpenseActivity extends AppCompatActivity {

    private ActivityExpenseBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExpenseAdapter adapter;
    private CollectionReference expenseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            expenseRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("expenses");
            setupRecyclerView();
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void setupRecyclerView() {
        Query query = expenseRef.orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ExpenseModel> options = new FirestoreRecyclerOptions.Builder<ExpenseModel>()
                .setQuery(query, ExpenseModel.class)
                .build();

        adapter = new ExpenseAdapter(options);
        binding.expenseRecyclerView.setHasFixedSize(true);
        binding.expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.expenseRecyclerView.setAdapter(adapter);
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_expense, null);
        builder.setView(dialogView)
                .setTitle("Add Expense")
                .setPositiveButton("Save", (dialog, which) -> {
                    EditText titleEt = dialogView.findViewById(R.id.title_edit_text);
                    EditText amountEt = dialogView.findViewById(R.id.amount_edit_text);
                    EditText categoryEt = dialogView.findViewById(R.id.category_edit_text);

                    String title = titleEt.getText().toString().trim();
                    String amountStr = amountEt.getText().toString().trim();
                    String category = categoryEt.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(this, "Title and Amount are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    ExpenseModel expense = new ExpenseModel(title, amount, category);

                    expenseRef.add(expense)
                            .addOnSuccessListener(documentReference -> Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
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
