package com.easydokan.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.easydokan.adapters.SaleItemAdapter;
import com.easydokan.models.CustomerModel;
import com.easydokan.models.ProductModel;
import com.easydokan.models.SaleItem;
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

public class SalesActivity extends AppCompatActivity {

    // UI Elements
    private Toolbar toolbar;
    private AutoCompleteTextView customerAutocomplete, productAutocomplete;
    private ImageButton addCustomerButton, addProductButton;
    private TextView previousDueTextView;
    private EditText quantityEditText;
    private MaterialButton addToBillButton;
    private RecyclerView salesBillRecyclerView;
    private TextView subtotalTextView, totalTextView, dueTextView;
    private EditText discountEditText, paidAmountEditText;
    private MaterialButton saveSaleButton, cancelButton;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference customerRef, productRef, salesRef;

    // Data & Adapters
    private List<CustomerModel> customerList;
    private ArrayAdapter<CustomerModel> customerAdapter;
    private List<ProductModel> productList;
    private ArrayAdapter<ProductModel> productAdapter;
    private List<SaleItem> saleItems;
    private SaleItemAdapter saleItemAdapter;

    private CustomerModel selectedCustomer;
    private ProductModel selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        initFirebase();
        initViews();
        setupListeners();
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
            salesRef = db.collection("users").document(userId).collection("sales");
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Sale");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        customerAutocomplete = findViewById(R.id.customer_autocomplete);
        productAutocomplete = findViewById(R.id.product_autocomplete);
        addCustomerButton = findViewById(R.id.add_customer_button);
        addProductButton = findViewById(R.id.add_product_button);
        previousDueTextView = findViewById(R.id.previous_due_textview);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addToBillButton = findViewById(R.id.add_to_bill_button);
        salesBillRecyclerView = findViewById(R.id.sales_bill_recyclerview);
        subtotalTextView = findViewById(R.id.subtotal_textview);
        totalTextView = findViewById(R.id.total_textview);
        dueTextView = findViewById(R.id.due_textview);
        discountEditText = findViewById(R.id.discount_edit_text);
        paidAmountEditText = findViewById(R.id.paid_amount_edit_text);
        saveSaleButton = findViewById(R.id.save_sale_button);
        cancelButton = findViewById(R.id.cancel_button);

        customerList = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerList);
        customerAutocomplete.setAdapter(customerAdapter);

        productList = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productList);
        productAutocomplete.setAdapter(productAdapter);

        saleItems = new ArrayList<>();
        salesBillRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        saleItemAdapter = new SaleItemAdapter(saleItems, position -> {
            saleItems.remove(position);
            saleItemAdapter.notifyItemRemoved(position);
            calculateTotals();
        });
        salesBillRecyclerView.setAdapter(saleItemAdapter);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        cancelButton.setOnClickListener(v -> finish());
        saveSaleButton.setOnClickListener(v -> saveSale());
        addCustomerButton.setOnClickListener(v -> showAddCustomerDialog());
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        customerAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomer = customerList.get(position);
            updateDueBalanceDisplay();
        });

        productAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = productList.get(position);
        });

        addToBillButton.setOnClickListener(v -> addToBill());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotals();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        discountEditText.addTextChangedListener(textWatcher);
        paidAmountEditText.addTextChangedListener(textWatcher);
    }

    private void showAddCustomerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer_sale, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.customer_name_edit_text_dialog);
        final EditText phoneEt = dialogView.findViewById(R.id.customer_phone_edit_text_dialog);
        final EditText addressEt = dialogView.findViewById(R.id.customer_address_edit_text_dialog);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Customer name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            CustomerModel newCustomer = new CustomerModel();
            newCustomer.setName(name);
            newCustomer.setPhone(phoneEt.getText().toString());
            newCustomer.setAddress(addressEt.getText().toString());
            newCustomer.setDue(0);

            customerRef.add(newCustomer).addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Customer Added", Toast.LENGTH_SHORT).show();
                loadCustomers();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product_sale, null);
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
            // Note: searchKeywords will be generated when product is saved from ProductActivity

            productRef.add(newProduct).addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show();
                loadProducts();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveSale() {
        if (selectedCustomer == null) {
            Toast.makeText(this, "Please select a customer.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (saleItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one item to the bill.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Sale");
        progressDialog.show();

        double subtotal = Double.parseDouble(subtotalTextView.getText().toString().replace("৳ ", ""));
        double discount = Double.parseDouble(discountEditText.getText().toString().isEmpty() ? "0" : discountEditText.getText().toString());
        double total = subtotal - discount;
        double paidAmount = Double.parseDouble(paidAmountEditText.getText().toString().isEmpty() ? "0" : paidAmountEditText.getText().toString());
        double saleDueAmount = total - paidAmount;

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentReference customerDocRef = customerRef.document(selectedCustomer.getId());
            DocumentSnapshot customerSnap = transaction.get(customerDocRef);
            double currentDue = customerSnap.getDouble("due");
            double newTotalDue = currentDue + saleDueAmount;
            transaction.update(customerDocRef, "due", newTotalDue);

            for (SaleItem item : saleItems) {
                DocumentReference productDocRef = productRef.document(item.getProductId());
                DocumentSnapshot productSnap = transaction.get(productDocRef);
                long currentStock = productSnap.getLong("stock");
                if (currentStock < item.getQuantity()) {
                    throw new FirebaseFirestoreException(item.getProductName() + " is out of stock.", FirebaseFirestoreException.Code.ABORTED);
                }
                transaction.update(productDocRef, "stock", currentStock - item.getQuantity());
            }

            Map<String, Object> saleData = new HashMap<>();
            saleData.put("customerId", selectedCustomer.getId());
            saleData.put("customerName", selectedCustomer.getName());
            saleData.put("items", saleItems);
            saleData.put("subtotal", subtotal);
            saleData.put("discount", discount);
            saleData.put("total", total);
            saleData.put("paidAmount", paidAmount);
            saleData.put("dueAmount", saleDueAmount);
            saleData.put("createdAt", FieldValue.serverTimestamp());

            transaction.set(salesRef.document(), saleData);

            return null;
        }).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale saved successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void addToBill() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product.", Toast.LENGTH_SHORT).show();
            return;
        }
        String quantityStr = quantityEditText.getText().toString();
        if (TextUtils.isEmpty(quantityStr) || Integer.parseInt(quantityStr) <= 0) {
            Toast.makeText(this, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(quantityStr);

        for (SaleItem item : saleItems) {
            if (item.getProductId().equals(selectedProduct.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                saleItemAdapter.notifyDataSetChanged();
                calculateTotals();
                clearProductSelection();
                return;
            }
        }

        SaleItem newItem = new SaleItem(selectedProduct.getId(), selectedProduct.getName(), selectedProduct.getPrice(), quantity);
        saleItems.add(newItem);
        saleItemAdapter.notifyDataSetChanged();
        calculateTotals();
        clearProductSelection();
    }

    private void clearProductSelection() {
        productAutocomplete.setText("");
        quantityEditText.setText("");
        selectedProduct = null;
        productAutocomplete.requestFocus();
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (SaleItem item : saleItems) {
            subtotal += item.getSubtotal();
        }
        subtotalTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", subtotal));

        double discount = 0;
        String discountStr = discountEditText.getText().toString();
        if (!discountStr.isEmpty()) {
            try {
                discount = Double.parseDouble(discountStr);
            } catch (NumberFormatException e) { /* ignore */ }
        }

        double total = subtotal - discount;
        totalTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", total));

        double paidAmount = 0;
        String paidStr = paidAmountEditText.getText().toString();
        if (!paidStr.isEmpty()) {
            try {
                paidAmount = Double.parseDouble(paidStr);
            } catch (NumberFormatException e) { /* ignore */ }
        }

        double due = total - paidAmount;
        dueTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", due));
    }

    private void updateDueBalanceDisplay() {
        if (selectedCustomer != null && selectedCustomer.getDue() > 0) {
            previousDueTextView.setText(String.format(Locale.getDefault(), "Previous Due: ৳ %.2f", selectedCustomer.getDue()));
            previousDueTextView.setVisibility(View.VISIBLE);
        } else {
            previousDueTextView.setVisibility(View.GONE);
        }
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
                Toast.makeText(this, "Failed to load customers.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
