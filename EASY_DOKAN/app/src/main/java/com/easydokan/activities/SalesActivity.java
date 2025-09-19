package com.easydokan.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AutoCompleteTextView customerAutocomplete, productAutocomplete;
    private TextView previousDueTextView;
    private EditText quantityEditText;
    private RecyclerView salesBillRecyclerView;
    private SaleItemAdapter saleItemAdapter;
    private List<SaleItem> saleItems;
    private TextView subtotalTextView, totalTextView, dueTextView;
    private EditText discountEditText, paidAmountEditText;
    private RadioGroup paymentMethodRadioGroup;
    private MaterialButton saveSaleButton;

    private FirebaseFirestore db;
    private CollectionReference customerRef, productRef, salesRef;

    private List<CustomerModel> customerList;
    private ArrayAdapter<CustomerModel> customerAdapter;
    private List<ProductModel> productList;
    private ArrayAdapter<ProductModel> productAdapter;

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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
        previousDueTextView = findViewById(R.id.previous_due_textview);
        customerList = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerList);
        customerAutocomplete.setAdapter(customerAdapter);

        productAutocomplete = findViewById(R.id.product_autocomplete);
        productList = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productList);
        productAutocomplete.setAdapter(productAdapter);
        quantityEditText = findViewById(R.id.quantity_edit_text);

        salesBillRecyclerView = findViewById(R.id.sales_bill_recyclerview);
        salesBillRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        saleItems = new ArrayList<>();
        saleItemAdapter = new SaleItemAdapter(saleItems, position -> {
            saleItems.remove(position);
            saleItemAdapter.notifyItemRemoved(position);
            calculateTotals();
        });
        salesBillRecyclerView.setAdapter(saleItemAdapter);

        subtotalTextView = findViewById(R.id.subtotal_textview);
        discountEditText = findViewById(R.id.discount_edit_text);
        totalTextView = findViewById(R.id.total_textview);
        paidAmountEditText = findViewById(R.id.paid_amount_edit_text);
        dueTextView = findViewById(R.id.due_textview);
        paymentMethodRadioGroup = findViewById(R.id.payment_method_radiogroup);
        saveSaleButton = findViewById(R.id.save_sale_button);
        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());
        findViewById(R.id.add_to_bill_button).setOnClickListener(v -> addToBill());
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        customerAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomer = customerList.get(position);
            updateDueBalanceDisplay();
        });
        productAutocomplete.setOnItemClickListener((parent, view, position, id) -> selectedProduct = productList.get(position));

        findViewById(R.id.add_customer_button).setOnClickListener(v -> showAddCustomerDialog());
        findViewById(R.id.add_product_button).setOnClickListener(v -> showAddProductDialog());

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { calculateTotals(); }
            public void afterTextChanged(android.text.Editable s) {}
        };
        discountEditText.addTextChangedListener(textWatcher);
        paidAmountEditText.addTextChangedListener(textWatcher);

        saveSaleButton.setOnClickListener(v -> saveSale());
    }

    private void saveSale() {
        if (selectedCustomer == null || saleItems.isEmpty()) {
            Toast.makeText(this, "Please select a customer and add items.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Sale");
        progressDialog.show();

        double subtotal = saleItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
        double discount = Double.parseDouble(discountEditText.getText().toString().isEmpty() ? "0" : discountEditText.getText().toString());
        double totalAmount = subtotal - discount;
        double paidAmount = Double.parseDouble(paidAmountEditText.getText().toString().isEmpty() ? "0" : paidAmountEditText.getText().toString());
        double dueAmount = totalAmount - paidAmount;

        DocumentReference customerDocRef = customerRef.document(selectedCustomer.getId());

        Map<String, Object> saleData = new HashMap<>();
        saleData.put("customer_id", customerDocRef);
        saleData.put("items", saleItems);
        saleData.put("total_amount", totalAmount);
        saleData.put("paid_amount", paidAmount);
        saleData.put("due_amount", dueAmount);
        saleData.put("payment_method", getSelectedPaymentMethod());
        saleData.put("created_at", com.google.firebase.firestore.FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();

        // 1. Create Sale Record
        batch.set(salesRef.document(), saleData);

        // 2. Update Customer's total_due
        batch.update(customerDocRef, "total_due", com.google.firebase.firestore.FieldValue.increment(dueAmount));
        batch.update(customerDocRef, "updated_at", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // 3. Update Product Stock
        for (SaleItem item : saleItems) {
            DocumentReference productDocRef = productRef.document(item.getProductId());
            batch.update(productDocRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
            batch.update(productDocRef, "last_updated", com.google.firebase.firestore.FieldValue.serverTimestamp());
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale saved successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void addToBill() {
        if (selectedProduct == null || quantityEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select a product and enter quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(quantityEditText.getText().toString());

        saleItems.add(new SaleItem(selectedProduct.getId(), selectedProduct.getName(), selectedProduct.getPrice(), quantity));
        saleItemAdapter.notifyItemInserted(saleItems.size() - 1);
        calculateTotals();
        clearProductSelection();
    }

    private void calculateTotals() {
        double subtotal = saleItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
        subtotalTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", subtotal));

        double discount = Double.parseDouble(discountEditText.getText().toString().isEmpty() ? "0" : discountEditText.getText().toString());
        double total = subtotal - discount;
        totalTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", total));

        double paidAmount = Double.parseDouble(paidAmountEditText.getText().toString().isEmpty() ? "0" : paidAmountEditText.getText().toString());
        double due = total - paidAmount;
        dueTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", due));
    }

    private void updateDueBalanceDisplay() {
        if (selectedCustomer != null && selectedCustomer.getTotal_due() > 0) {
            previousDueTextView.setText(String.format(Locale.getDefault(), "Previous Due: ৳ %.2f", selectedCustomer.getTotal_due()));
            previousDueTextView.setVisibility(View.VISIBLE);
        } else {
            previousDueTextView.setVisibility(View.GONE);
        }
    }

    private void loadCustomers() {
        customerRef.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
            customerList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                CustomerModel customer = document.toObject(CustomerModel.class);
                customer.setId(document.getId());
                customerList.add(customer);
            }
            customerAdapter.notifyDataSetChanged();
        });
    }

    private void loadProducts() {
        productRef.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
            productList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ProductModel product = document.toObject(ProductModel.class);
                product.setId(document.getId());
                productList.add(product);
            }
            productAdapter.notifyDataSetChanged();
        });
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentMethodRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_cash) return "Cash";
        if (selectedId == R.id.radio_bkash) return "Bkash";
        // Add other payment methods if any
        return "Other";
    }

    private void clearProductSelection() {
        productAutocomplete.setText("");
        quantityEditText.setText("");
        selectedProduct = null;
        productAutocomplete.requestFocus();
    }

    private void showAddCustomerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_customer, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        final EditText phoneEt = dialogView.findViewById(R.id.phone_edit_text);
        final EditText addressEt = dialogView.findViewById(R.id.address_edit_text);
        final EditText totalDueEt = dialogView.findViewById(R.id.total_due_edit_text);

        builder.setTitle(R.string.add_new_customer);

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
            try {
                customer.setTotal_due(Double.parseDouble(totalDueEt.getText().toString()));
            } catch (NumberFormatException e) {
                customer.setTotal_due(0.0);
            }

            customerRef.add(customer).addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Customer added", Toast.LENGTH_SHORT).show();
                loadCustomers(); // Refresh the list
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        final EditText categoryEt = dialogView.findViewById(R.id.category_edit_text);
        final EditText unitEt = dialogView.findViewById(R.id.unit_edit_text);
        final EditText priceEt = dialogView.findViewById(R.id.price_edit_text);
        final EditText stockEt = dialogView.findViewById(R.id.stock_edit_text);

        builder.setTitle(R.string.add_new_product);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            ProductModel product = new ProductModel();
            product.setName(name);
            product.setCategory(categoryEt.getText().toString());
            product.setUnit(unitEt.getText().toString());
            product.setPrice(Double.parseDouble(priceEt.getText().toString().isEmpty() ? "0" : priceEt.getText().toString()));
            product.setStock(Long.parseLong(stockEt.getText().toString().isEmpty() ? "0" : stockEt.getText().toString()));
            product.setAdded_by(FirebaseAuth.getInstance().getCurrentUser().getUid());

            productRef.add(product).addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                loadProducts(); // Refresh the list
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
