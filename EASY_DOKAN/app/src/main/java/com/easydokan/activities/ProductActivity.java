package com.easydokan.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.easydokan.R;
import com.easydokan.adapters.ProductAdapter;
import com.easydokan.databinding.ActivityProductBinding;
import com.easydokan.models.ProductModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ProductActivity extends AppCompatActivity {

    private ActivityProductBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private ProductAdapter adapter;
    private CollectionReference productRef;

    private Uri imageUri;
    private ImageView dialogProductImage;
    private ActivityResultLauncher<String> mGetContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            productRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("products");
            setupRecyclerView();
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddProduct.setOnClickListener(v -> showAddEditProductDialog(null));
        setupSearch();
        registerImagePicker();
    }

    private void registerImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        if (dialogProductImage != null) {
                            dialogProductImage.setImageURI(imageUri);
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        Query query = productRef.orderBy("name", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class)
                .build();

        adapter = new ProductAdapter(options);
        binding.productRecyclerView.setHasFixedSize(true);
        binding.productRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.productRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(DocumentSnapshot documentSnapshot) {
                showAddEditProductDialog(documentSnapshot);
            }

            @Override
            public void onDeleteClick(DocumentSnapshot documentSnapshot) {
                new AlertDialog.Builder(ProductActivity.this)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete this product?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteProduct(documentSnapshot))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void deleteProduct(DocumentSnapshot snapshot) {
        ProductModel product = snapshot.toObject(ProductModel.class);
        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Delete image from storage
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.getImageUrl());
            imageRef.delete().addOnSuccessListener(aVoid -> {
                // Image deleted, now delete firestore document
                snapshot.getReference().delete();
                Toast.makeText(ProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(ProductActivity.this, "Error deleting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // No image, just delete firestore document
            snapshot.getReference().delete();
            Toast.makeText(ProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddEditProductDialog(DocumentSnapshot snapshot) {
        imageUri = null; // Reset image uri for each dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);

        dialogProductImage = dialogView.findViewById(R.id.product_image_view);
        Button selectImageButton = dialogView.findViewById(R.id.select_image_button);
        EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        EditText categoryEt = dialogView.findViewById(R.id.category_edit_text);
        EditText priceEt = dialogView.findViewById(R.id.price_edit_text);
        EditText quantityEt = dialogView.findViewById(R.id.quantity_edit_text);
        EditText supplierEt = dialogView.findViewById(R.id.supplier_edit_text);

        selectImageButton.setOnClickListener(v -> mGetContent.launch("image/*"));

        ProductModel existingProduct = null;
        if (snapshot != null) {
            builder.setTitle("Edit Product");
            existingProduct = snapshot.toObject(ProductModel.class);
            nameEt.setText(existingProduct.getName());
            categoryEt.setText(existingProduct.getCategory());
            priceEt.setText(String.valueOf(existingProduct.getPrice()));
            quantityEt.setText(String.valueOf(existingProduct.getQuantity()));
            supplierEt.setText(existingProduct.getSupplier());
            if (existingProduct.getImageUrl() != null) {
                Glide.with(this).load(existingProduct.getImageUrl()).into(dialogProductImage);
            }
        } else {
            builder.setTitle("Add Product");
        }

        ProductModel finalExistingProduct = existingProduct;
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Validation
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            // Add other validations...

            uploadImageAndSaveProduct(name, categoryEt.getText().toString(),
                    priceEt.getText().toString(), quantityEt.getText().toString(),
                    supplierEt.getText().toString(), finalExistingProduct);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void uploadImageAndSaveProduct(String name, String category, String priceStr, String quantityStr, String supplier, ProductModel existingProduct) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        if (imageUri != null) {
            // New image selected, upload it
            StorageReference fileRef = storageRef.child("product_images/" + mAuth.getCurrentUser().getUid() + "/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Delete old image if updating
                    if(existingProduct != null && existingProduct.getImageUrl() != null) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(existingProduct.getImageUrl()).delete();
                    }
                    saveProductToFirestore(name, category, priceStr, quantityStr, supplier, imageUrl, existingProduct, progressDialog);
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            // No new image, use existing one or none
            String imageUrl = (existingProduct != null) ? existingProduct.getImageUrl() : null;
            saveProductToFirestore(name, category, priceStr, quantityStr, supplier, imageUrl, existingProduct, progressDialog);
        }
    }

    private void saveProductToFirestore(String name, String category, String priceStr, String quantityStr, String supplier, String imageUrl, ProductModel existingProduct, ProgressDialog progressDialog) {
        double price = TextUtils.isEmpty(priceStr) ? 0 : Double.parseDouble(priceStr);
        long quantity = TextUtils.isEmpty(quantityStr) ? 0 : Long.parseLong(quantityStr);

        ProductModel product = new ProductModel(name, category, price, quantity, supplier, imageUrl);

        if (existingProduct != null) {
            // Update
            productRef.document(existingProduct.getId()).set(product).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                Toast.makeText(ProductActivity.this, "Product updated", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Create
            productRef.add(product).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                Toast.makeText(ProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
    }

    private void performSearch(String text) {
        Query query;
        if (text.isEmpty()) {
            query = productRef.orderBy("name", Query.Direction.ASCENDING);
        } else {
            query = productRef.orderBy("name").startAt(text).endAt(text + "\uf8ff");
        }

        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class)
                .build();

        adapter.updateOptions(options);
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
