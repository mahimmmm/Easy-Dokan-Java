package com.easydokan.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.easydokan.R;
import com.easydokan.adapters.CartAdapter;
import com.easydokan.adapters.ProductSelectionAdapter;
import com.easydokan.databinding.ActivityNewSaleBinding;
import com.easydokan.databinding.DialogSelectProductBinding;
import com.easydokan.models.CustomerModel;
import com.easydokan.models.ProductModel;
import com.easydokan.models.SaleItemModel;
import com.easydokan.models.SaleModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewSaleActivity extends AppCompatActivity {

    private ActivityNewSaleBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference customerRef, productRef, salesRef;

    private List<CustomerModel> customerList = new ArrayList<>();
    private CustomerModel selectedCustomer;
    private List<SaleItemModel> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewSaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupCustomerAutoComplete();
        setupCartRecyclerView();

        binding.selectProductsButton.setOnClickListener(v -> showProductSelectionDialog());
        binding.saveSaleButton.setOnClickListener(v -> saveSale());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
        customerRef = db.collection("users").document(userId).collection("customers");
        productRef = db.collection("users").document(userId).collection("products");
        salesRef = db.collection("users").document(userId).collection("sales");
    }

    private void setupCustomerAutoComplete() {
        customerRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> customerNames = new ArrayList<>();
            for (CustomerModel customer : queryDocumentSnapshots.toObjects(CustomerModel.class)) {
                customerList.add(customer);
                customerNames.add(customer.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, customerNames);
            binding.customerAutocomplete.setAdapter(adapter);
            binding.customerAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
                String selectedName = (String) parent.getItemAtPosition(position);
                for(CustomerModel customer : customerList) {
                    if(customer.getName().equals(selectedName)) {
                        selectedCustomer = customer;
                        selectedCustomer.setId(queryDocumentSnapshots.getDocuments().get(customerNames.indexOf(selectedName)).getId());
                        break;
                    }
                }
            });
        });
    }

    private void setupCartRecyclerView() {
        cartAdapter = new CartAdapter(cartItems);
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.cartRecyclerView.setAdapter(cartAdapter);
        cartAdapter.setOnItemRemoveListener(position -> {
            cartItems.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateTotal();
        });
    }

    private void showProductSelectionDialog() {
        DialogSelectProductBinding dialogBinding = DialogSelectProductBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        Query query = productRef.orderBy("name");
        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class).build();
        ProductSelectionAdapter productAdapter = new ProductSelectionAdapter(options);
        dialogBinding.productSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.productSelectionRecyclerView.setAdapter(productAdapter);
        productAdapter.startListening();

        productAdapter.setOnItemClickListener(product -> {
            dialog.dismiss();
            showQuantityDialog(product);
        });

        dialog.setOnDismissListener(d -> productAdapter.stopListening());
        dialog.show();
    }

    private void showQuantityDialog(ProductModel product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Quantity");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            long quantity = Long.parseLong(input.getText().toString());
            if (quantity > 0 && quantity <= product.getQuantity()) {
                SaleItemModel saleItem = new SaleItemModel(product.getId(), product.getName(), quantity, product.getPrice());
                cartItems.add(saleItem);
                cartAdapter.notifyItemInserted(cartItems.size() - 1);
                updateTotal();
            } else {
                Toast.makeText(this, "Invalid quantity or not enough stock", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateTotal() {
        double total = 0;
        for (SaleItemModel item : cartItems) {
            total += item.getSubtotal();
        }
        binding.totalAmountText.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
    }

    private void saveSale() {
        if (selectedCustomer == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Please select a customer and add products", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = 0;
        for (SaleItemModel item : cartItems) {
            totalAmount += item.getSubtotal();
        }

        String paymentMode = "Cash"; // Default
        int checkedId = binding.paymentMethodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_card) paymentMode = "Card";
        else if (checkedId == R.id.radio_mobile) paymentMode = "Mobile Banking";

        SaleModel sale = new SaleModel(selectedCustomer.getId(), selectedCustomer.getName(), totalAmount, paymentMode);

        WriteBatch batch = db.batch();

        // 1. Add sale to sales collection
        DocumentReference saleDocRef = salesRef.document();
        batch.set(saleDocRef, sale);

        // 2. Add sale items to subcollection and update product quantities
        for(SaleItemModel item : cartItems) {
            DocumentReference itemDocRef = saleDocRef.collection("items").document(item.getProductId());
            batch.set(itemDocRef, item);

            DocumentReference productDocRef = productRef.document(item.getProductId());
            batch.update(productDocRef, "quantity", com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Sale saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error saving sale: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
