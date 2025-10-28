package com.easydokan.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easydokan.databinding.ActivityCustomerDetailBinding;
import com.easydokan.models.CustomerModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.Locale;

public class CustomerDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CUSTOMER_ID = "extra_customer_id";
    private ActivityCustomerDetailBinding binding;
    private FirebaseFirestore db;
    private DocumentReference customerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String customerId = getIntent().getStringExtra(EXTRA_CUSTOMER_ID);
        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "Customer ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initFirebase(customerId);
        setupToolbar();
        loadCustomerData();
    }

    private void initFirebase(String customerId) {
        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerRef = db.collection("users").document(uid).collection("customers").document(customerId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbarCustomerDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarCustomerDetail.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCustomerData() {
        customerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                CustomerModel customer = documentSnapshot.toObject(CustomerModel.class);
                if (customer != null) {
                    binding.toolbarCustomerDetail.setTitle(customer.getName());
                    binding.detailCustomerName.setText(customer.getName());
                    binding.detailCustomerPhone.setText(customer.getPhone());
                    binding.detailCustomerAddress.setText(customer.getAddress());
                    binding.detailTotalDue.setText(String.format("Total Due\n%s", formatCurrency(customer.getTotal_due())));
                    // TODO: Calculate and display total business
                    binding.detailTotalBusiness.setText("Total Business\n-");
                }
            } else {
                Toast.makeText(this, "Customer not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load customer data.", Toast.LENGTH_SHORT).show();
        });
    }

    private String formatCurrency(double amount) {
        try {
            return NumberFormat.getCurrencyInstance(new Locale("bn", "BD")).format(amount);
        } catch (Exception e) {
            return NumberFormat.getCurrencyInstance().format(amount);
        }
    }
}
