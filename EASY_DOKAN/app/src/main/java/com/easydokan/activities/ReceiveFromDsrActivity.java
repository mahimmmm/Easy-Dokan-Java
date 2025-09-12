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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceiveFromDsrActivity extends AppCompatActivity {

    // UI Elements
    private Toolbar toolbar;
    private AutoCompleteTextView dsrAutocomplete, productAutocomplete;
    private ImageButton addDsrButton, addProductButton;
    private EditText quantityEditText, unitPriceEditText;
    private MaterialButton addToReceiveListButton, saveReceiveButton, cancelButton;
    private RecyclerView receiveListRecyclerView;
    private TextView totalAmountTextView;
    private EditText notesEditText;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference dsrRef, productRef, receiveRef;

    // Adapters and Data
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

        addDsrButton = findViewById(R.id.add_dsr_button);
        addProductButton = findViewById(R.id.add_product_button_receive);
        quantityEditText = findViewById(R.id.quantity_edit_text_receive);
        unitPriceEditText = findViewById(R.id.unit_price_edit_text_receive);
        addToReceiveListButton = findViewById(R.id.add_to_receive_list_button);

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
        saveReceiveButton = findViewById(R.id.save_receive_button);
        cancelButton = findViewById(R.id.cancel_button_receive);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        dsrAutocomplete.setOnItemClickListener((parent, view, position, id) -> selectedDsr = (DsrModel) parent.getItemAtPosition(position));
        productAutocomplete.setOnItemClickListener((parent, view, position, id) -> selectedProduct = (ProductModel) parent.getItemAtPosition(position));
        addDsrButton.setOnClickListener(v -> showAddDsrDialog());
        addProductButton.setOnClickListener(v -> showAddProductDialog());
        addToReceiveListButton.setOnClickListener(v -> addToReceiveList());
        saveReceiveButton.setOnClickListener(v -> saveReceiveEntry());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void showAddDsrDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_dsr, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.dsr_name_edit_text);
        final EditText phoneEt = dialogView.findViewById(R.id.dsr_phone_edit_text);
        final EditText companyEt = dialogView.findViewById(R.id.dsr_company_edit_text);
        final EditText addressEt = dialogView.findViewById(R.id.dsr_address_edit_text);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "DSR name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            DsrModel newDsr = new DsrModel();
            newDsr.setName(name);
            newDsr.setPhone(phoneEt.getText().toString());
            newDsr.setCompany(companyEt.getText().toString());
            newDsr.setAddress(addressEt.getText().toString());

            dsrRef.add(newDsr).addOnSuccessListener(docRef -> {
                Toast.makeText(this, "DSR added", Toast.LENGTH_SHORT).show();
                loadDsrList();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error adding DSR", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product_receive, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.product_name_edit_text_dialog);
        final EditText categoryEt = dialogView.findViewById(R.id.product_category_edit_text_dialog);
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
            newProduct.setPrice(Double.parseDouble(priceEt.getText().toString().isEmpty() ? "0" : priceEt.getText().toString()));
            newProduct.setStock(Long.parseLong(stockEt.getText().toString().isEmpty() ? "0" : stockEt.getText().toString()));

            productRef.add(newProduct).addOnSuccessListener(docRef -> {
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                loadProductList();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error adding product", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveReceiveEntry() {
        if (selectedDsr == null) {
            Toast.makeText(this, "Please select a DSR.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (receiveItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one product.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Receive Entry");
        progressDialog.show();

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            for (ReceiveItem item : receiveItems) {
                DocumentReference productDocRef = productRef.document(item.getProductId());
                DocumentSnapshot productSnap = transaction.get(productDocRef);
                if (!productSnap.exists()) throw new FirebaseFirestoreException("Product not found: " + item.getProductName(), FirebaseFirestoreException.Code.ABORTED);
                long currentStock = productSnap.getLong("stock");
                transaction.update(productDocRef, "stock", currentStock + item.getQuantity());
            }
            Map<String, Object> receiveData = new HashMap<>();
            receiveData.put("dsrId", selectedDsr.getId());
            receiveData.put("dsrName", selectedDsr.getName());
            receiveData.put("products", receiveItems);
            receiveData.put("totalAmount", Double.parseDouble(totalAmountTextView.getText().toString().replace("৳ ", "")));
            receiveData.put("notes", notesEditText.getText().toString());
            receiveData.put("createdAt", FieldValue.serverTimestamp());
            transaction.set(receiveRef.document(), receiveData);
            return null;
        }).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Receive entry saved successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void addToReceiveList() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }
        String qtyStr = quantityEditText.getText().toString();
        String priceStr = unitPriceEditText.getText().toString();
        if (qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity and unit price", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(qtyStr);
        double unitPrice = Double.parseDouble(priceStr);
        for (ReceiveItem item : receiveItems) {
            if (item.getProductId().equals(selectedProduct.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                item.setUnitPrice(unitPrice);
                receiveItemAdapter.notifyDataSetChanged();
                calculateTotalAmount();
                clearProductInput();
                return;
            }
        }
        ReceiveItem newItem = new ReceiveItem(selectedProduct.getId(), selectedProduct.getName(), quantity, unitPrice);
        receiveItems.add(newItem);
        receiveItemAdapter.notifyDataSetChanged();
        calculateTotalAmount();
        clearProductInput();
    }

    private void calculateTotalAmount() {
        double total = 0;
        for (ReceiveItem item : receiveItems) {
            total += item.getSubtotal();
        }
        totalAmountTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", total));
    }

    private void clearProductInput() {
        productAutocomplete.setText("");
        quantityEditText.setText("");
        unitPriceEditText.setText("");
        selectedProduct = null;
        productAutocomplete.requestFocus();
    }

    private void loadDsrList() {
        if (dsrRef == null) return;
        dsrRef.orderBy("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dsrList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DsrModel dsr = document.toObject(DsrModel.class);
                    dsr.setId(document.getId());
                    dsrList.add(dsr);
                }
                dsrAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load DSR list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductList() {
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
                Toast.makeText(this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
