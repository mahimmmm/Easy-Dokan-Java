package com.easydokan.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton addCustomerButton, addProductButton; // Corrected Type
    private MaterialButton addToBillButton;
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
        // setupListeners(); // Listeners will be added in a future step

        // loadCustomers(); // Data loading will be added in a future step
        // loadProducts();
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
        addCustomerButton = findViewById(R.id.add_customer_button); // This will now work
        previousDueTextView = findViewById(R.id.previous_due_textview);

        // Product
        productAutocomplete = findViewById(R.id.product_autocomplete);
        addProductButton = findViewById(R.id.add_product_button); // This will now work
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addToBillButton = findViewById(R.id.add_to_bill_button);

        // Bill
        salesBillRecyclerView = findViewById(R.id.sales_bill_recyclerview);

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

    // The rest of the logic will be added incrementally in future steps.
}
