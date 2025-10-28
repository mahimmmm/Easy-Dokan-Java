package com.easydokan.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.adapters.ReceiveItemAdapter;
import com.easydokan.models.DsrModel;
import com.easydokan.models.ProductModel;
import com.easydokan.models.ReceiveItem;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceiveFromDsrActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AutoCompleteTextView dsrAutocomplete, productAutocomplete;
    private EditText quantityEditText, unitPriceEditText;
    private RecyclerView receiveListRecyclerView;
    private TextView totalAmountTextView;
    private EditText notesEditText;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference dsrRef, productRef, receiveRef;

    private List<DsrModel> dsrList;
    private ArrayAdapter<DsrModel> dsrAdapter;
    private List<ProductModel> productList;
    private ArrayAdapter<ProductModel> productAdapter;
    private List<ReceiveItem> receiveItems;
    private ReceiveItemAdapter receiveItemAdapter;

    private DsrModel selectedDsr;
    private ProductModel selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_from_dsr);
        initFirebase();
        initViews();
        setupListeners();
        loadDsrList();
        loadProductList();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            dsrRef = db.collection("users").document(userId).collection("dsrs");
            productRef = db.collection("users").document(userId).collection("products");
            receiveRef = db.collection("users").document(userId).collection("receiveFromDSR");
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_receive);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Receive From DSR");
        }
        dsrAutocomplete = findViewById(R.id.dsr_autocomplete);
        dsrList = new ArrayList<>();
        dsrAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dsrList);
        dsrAutocomplete.setAdapter(dsrAdapter);

        productAutocomplete = findViewById(R.id.product_autocomplete_receive);
        productList = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productList);
        productAutocomplete.setAdapter(productAdapter);

        quantityEditText = findViewById(R.id.quantity_edit_text_receive);
        unitPriceEditText = findViewById(R.id.unit_price_edit_text_receive);

        receiveListRecyclerView = findViewById(R.id.receive_list_recyclerview);
        receiveListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiveItems = new ArrayList<>();
        receiveItemAdapter = new ReceiveItemAdapter(receiveItems, position -> {
            receiveItems.remove(position);
            receiveItemAdapter.notifyItemRemoved(position);
            calculateTotalAmount();
        });
        receiveListRecyclerView.setAdapter(receiveItemAdapter);

        totalAmountTextView = findViewById(R.id.total_amount_textview_receive);
        notesEditText = findViewById(R.id.notes_edit_text_receive);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        dsrAutocomplete.setOnItemClickListener((parent, view, position, id) -> selectedDsr = dsrList.get(position));
        productAutocomplete.setOnItemClickListener((parent, view, position, id) -> selectedProduct = productList.get(position));
        findViewById(R.id.add_dsr_button).setOnClickListener(v -> showAddDsrDialog());
        findViewById(R.id.add_product_button_receive).setOnClickListener(v -> showAddProductDialog());
        findViewById(R.id.add_to_receive_list_button).setOnClickListener(v -> addToReceiveList());
        findViewById(R.id.save_receive_button).setOnClickListener(v -> saveReceiveEntry());
        findViewById(R.id.cancel_button_receive).setOnClickListener(v -> finish());
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product_receive, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.product_name_edit_text_dialog);
        final EditText categoryEt = dialogView.findViewById(R.id.product_category_edit_text_dialog);
        final EditText unitEt = dialogView.findViewById(R.id.product_unit_edit_text_dialog);
        final EditText priceEt = dialogView.findViewById(R.id.product_price_edit_text_dialog);
        final EditText stockEt = dialogView.findViewById(R.id.product_stock_edit_text_dialog);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            ProductModel newProduct = new ProductModel();
            newProduct.setName(name);
            newProduct.setCategory(categoryEt.getText().toString());
            newProduct.setUnit(unitEt.getText().toString());
            newProduct.setPrice(Double.parseDouble(priceEt.getText().toString().isEmpty() ? "0" : priceEt.getText().toString()));
            newProduct.setStock(Long.parseLong(stockEt.getText().toString().isEmpty() ? "0" : stockEt.getText().toString()));
            newProduct.setAdded_by(mAuth.getCurrentUser().getUid());

            productRef.add(newProduct).addOnSuccessListener(docRef -> {
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                loadProductList();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error adding product", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveReceiveEntry() {
        if (selectedDsr == null || receiveItems.isEmpty()) {
            Toast.makeText(this, "Please select a DSR and add items.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Receive Entry");
        progressDialog.show();

        WriteBatch batch = db.batch();
        for (ReceiveItem item : receiveItems) {
            DocumentReference productDocRef = productRef.document(item.getProductId());
            batch.update(productDocRef, "stock", FieldValue.increment(item.getQuantity()));
            batch.update(productDocRef, "last_updated", FieldValue.serverTimestamp());
        }

        Map<String, Object> receiveData = new HashMap<>();
        receiveData.put("dsr_name", selectedDsr.getName());
        receiveData.put("items", receiveItems);
        // ... create receive document data

        batch.set(receiveRef.document(), receiveData);

        batch.commit().addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Receive entry saved successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Other methods...
    private void showAddDsrDialog() { /* ... */ }
    private void addToReceiveList() { /* ... */ }
    private void calculateTotalAmount() { /* ... */ }
    private void clearProductInput() { /* ... */ }
    private void loadDsrList() { /* ... */ }
    private void loadProductList() { /* ... */ }
}
