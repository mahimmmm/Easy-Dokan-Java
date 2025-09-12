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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalesActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar toolbar;

    // Customer and Product Selection
    private AutoCompleteTextView customerAutocomplete, productAutocomplete;
    private MaterialButton addCustomerButton, addProductButton, addToBillButton;
    private TextView previousDueTextView;
    private EditText quantityEditText;

    // Bill Summary
    private RecyclerView salesBillRecyclerView;
    private SaleItemAdapter saleItemAdapter;
    private List<SaleItem> saleItems;

    // Totals
    private TextView subtotalTextView, totalTextView, dueTextView;
    private EditText discountEditText, paidAmountEditText;

    // Payment and Actions
    private RadioGroup paymentMethodRadioGroup;
    private MaterialButton saveSaleButton, cancelButton;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference customerRef, productRef, salesRef;

    // Data Lists
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
        // Toolbar
        toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Sale");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Customer
        customerAutocomplete = findViewById(R.id.customer_autocomplete);
        addCustomerButton = findViewById(R.id.add_customer_button);
        previousDueTextView = findViewById(R.id.previous_due_textview);
        customerList = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerList);
        customerAutocomplete.setAdapter(customerAdapter);

        // Product
        productAutocomplete = findViewById(R.id.product_autocomplete);
        addProductButton = findViewById(R.id.add_product_button);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addToBillButton = findViewById(R.id.add_to_bill_button);
        productList = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, productList);
        productAutocomplete.setAdapter(productAdapter);

        // Bill
        salesBillRecyclerView = findViewById(R.id.sales_bill_recyclerview);
        salesBillRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        saleItems = new ArrayList<>();
        saleItemAdapter = new SaleItemAdapter(saleItems, new SaleItemAdapter.OnItemInteractionListener() {
            @Override
            public void onDeleteClick(int position) {
                saleItems.remove(position);
                saleItemAdapter.notifyItemRemoved(position);
                calculateTotals();
            }
        });
        salesBillRecyclerView.setAdapter(saleItemAdapter);

        // Totals
        subtotalTextView = findViewById(R.id.subtotal_textview);
        discountEditText = findViewById(R.id.discount_edit_text);
        totalTextView = findViewById(R.id.total_textview);
        paidAmountEditText = findViewById(R.id.paid_amount_edit_text);
        dueTextView = findViewById(R.id.due_textview);

        // Payment and Actions
        paymentMethodRadioGroup = findViewById(R.id.payment_method_radiogroup);
        saveSaleButton = findViewById(R.id.save_sale_button);
        cancelButton = findViewById(R.id.cancel_button);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        customerAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomer = (CustomerModel) parent.getItemAtPosition(position);
            if (selectedCustomer != null) {
                updateDueBalanceDisplay();
            }
        });

        productAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = (ProductModel) parent.getItemAtPosition(position);
        });

        addToBillButton.setOnClickListener(v -> addToBill());

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotals();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        discountEditText.addTextChangedListener(textWatcher);
        paidAmountEditText.addTextChangedListener(textWatcher);

        saveSaleButton.setOnClickListener(v -> saveSale());
        cancelButton.setOnClickListener(v -> finish());
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

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Sale");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Prepare Sale Data
        double subtotal = 0;
        for (SaleItem item : saleItems) {
            subtotal += item.getSubtotal();
        }
        double discount = Double.parseDouble(discountEditText.getText().toString().isEmpty() ? "0" : discountEditText.getText().toString());
        double total = subtotal - discount;
        double paidAmount = Double.parseDouble(paidAmountEditText.getText().toString().isEmpty() ? "0" : paidAmountEditText.getText().toString());
        double dueAmount = total - paidAmount;

        java.util.Map<String, Object> saleData = new java.util.HashMap<>();
        saleData.put("customerId", selectedCustomer.getId());
        saleData.put("customerName", selectedCustomer.getName());
        saleData.put("items", saleItems); // Storing the list of items directly
        saleData.put("subtotal", subtotal);
        saleData.put("discount", discount);
        saleData.put("total", total);
        saleData.put("paidAmount", paidAmount);
        saleData.put("dueAmount", dueAmount);
        saleData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());


        db.runTransaction(transaction -> {
            // 1. Update Customer Due
            com.google.firebase.firestore.DocumentReference customerDocRef = customerRef.document(selectedCustomer.getId());
            com.google.firebase.firestore.DocumentSnapshot customerSnap = transaction.get(customerDocRef);
            double newDue = customerSnap.getDouble("due") + dueAmount;
            transaction.update(customerDocRef, "due", newDue);

            // 2. Update Product Stock
            for (SaleItem item : saleItems) {
                com.google.firebase.firestore.DocumentReference productDocRef = productRef.document(item.getProductId());
                com.google.firebase.firestore.DocumentSnapshot productSnap = transaction.get(productDocRef);
                long currentStock = productSnap.getLong("stock");
                if (currentStock < item.getQuantity()) {
                    throw new com.google.firebase.firestore.FirebaseFirestoreException("Not enough stock for " + item.getProductName(),
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
                }
                transaction.update(productDocRef, "stock", currentStock - item.getQuantity());
            }

            // 3. Create Sale Record
            transaction.set(salesRef.document(), saleData);

            return null; // Transaction success
        }).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale saved successfully!", Toast.LENGTH_LONG).show();
            finish(); // or clear form
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Sale failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void addToBill() {
        String quantityStr = quantityEditText.getText().toString();
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantityStr.isEmpty() || Integer.parseInt(quantityStr) <= 0) {
            Toast.makeText(this, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity = Integer.parseInt(quantityStr);

        // Check if item is already in the bill, if so, update quantity
        for (SaleItem item : saleItems) {
            if (item.getProductId().equals(selectedProduct.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                saleItemAdapter.notifyDataSetChanged();
                calculateTotals();
                clearProductSelection();
                return;
            }
        }

        // If not in the bill, add as a new item
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
            } catch (NumberFormatException e) {
                // Ignore invalid discount
            }
        }

        double total = subtotal - discount;
        totalTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", total));

        double paidAmount = 0;
        String paidAmountStr = paidAmountEditText.getText().toString();
        if (!paidAmountStr.isEmpty()) {
            try {
                paidAmount = Double.parseDouble(paidAmountStr);
            } catch (NumberFormatException e) {
                // Ignore invalid paid amount
            }
        }

        double dueAmount = total - paidAmount;
        dueTextView.setText(String.format(Locale.getDefault(), "৳ %.2f", dueAmount));
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

    // The rest of the logic (add to bill, calculate totals, save sale) will be added here
}
