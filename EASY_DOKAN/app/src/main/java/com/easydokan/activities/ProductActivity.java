package com.easydokan.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
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

        initFirebase();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            productRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("products");
            setupRecyclerView(productRef.orderBy("name", Query.Direction.ASCENDING));
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddProduct.setOnClickListener(v -> showAddEditProductDialog(null));
        registerImagePicker();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    private void registerImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageUri = uri;
                if (dialogProductImage != null) dialogProductImage.setImageURI(imageUri);
            }
        });
    }

    private void setupRecyclerView(Query query) {
        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class).build();
        adapter = new ProductAdapter(options);
        binding.productRecyclerView.setHasFixedSize(true);
        // Using a GridLayoutManager for the grid view
        binding.productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.productRecyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(DocumentSnapshot documentSnapshot) {
                showAddEditProductDialog(documentSnapshot);
            }
            @Override
            public void onDeleteClick(DocumentSnapshot documentSnapshot) {
                new AlertDialog.Builder(ProductActivity.this)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteProduct(documentSnapshot))
                        .setNegativeButton("Cancel", null).show();
            }
        });
    }

    private void deleteProduct(DocumentSnapshot snapshot) {
        ProductModel product = snapshot.toObject(ProductModel.class);
        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(product.getImageUrl());
            imageRef.delete().addOnSuccessListener(aVoid -> snapshot.getReference().delete());
        } else {
            snapshot.getReference().delete();
        }
        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
    }

    private void showAddEditProductDialog(DocumentSnapshot snapshot) {
        imageUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);

        dialogProductImage = dialogView.findViewById(R.id.product_image_view);
        dialogView.findViewById(R.id.select_image_button).setOnClickListener(v -> mGetContent.launch("image/*"));

        final EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        final EditText codeEt = dialogView.findViewById(R.id.code_edit_text);
        final EditText categoryEt = dialogView.findViewById(R.id.category_edit_text);
        final EditText priceEt = dialogView.findViewById(R.id.price_edit_text);
        final EditText stockEt = dialogView.findViewById(R.id.stock_edit_text);
        final EditText descEt = dialogView.findViewById(R.id.description_edit_text);

        ProductModel existingProduct = null;
        if (snapshot != null) {
            builder.setTitle(R.string.edit_product);
            existingProduct = snapshot.toObject(ProductModel.class);
            existingProduct.setId(snapshot.getId()); // Store ID for update
            nameEt.setText(existingProduct.getName());
            codeEt.setText(existingProduct.getCode());
            categoryEt.setText(existingProduct.getCategory());
            priceEt.setText(String.valueOf(existingProduct.getPrice()));
            stockEt.setText(String.valueOf(existingProduct.getStock()));
            descEt.setText(existingProduct.getDescription());
            if (existingProduct.getImageUrl() != null) {
                Glide.with(this).load(existingProduct.getImageUrl()).into(dialogProductImage);
            }
        } else {
            builder.setTitle(R.string.add_new_product);
        }

        ProductModel finalExistingProduct = existingProduct;
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameEt.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadImageAndSaveProduct(name, codeEt.getText().toString(), categoryEt.getText().toString(),
                    priceEt.getText().toString(), stockEt.getText().toString(), descEt.getText().toString(), finalExistingProduct);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void uploadImageAndSaveProduct(String name, String code, String category, String priceStr, String stockStr, String desc, ProductModel existingProduct) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Product...");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child("product_images/" + mAuth.getCurrentUser().getUid() + "/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if(existingProduct != null && existingProduct.getImageUrl() != null) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(existingProduct.getImageUrl()).delete();
                }
                saveProductToFirestore(name, code, category, priceStr, stockStr, desc, uri.toString(), existingProduct, progressDialog);
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            String imageUrl = (existingProduct != null) ? existingProduct.getImageUrl() : "";
            saveProductToFirestore(name, code, category, priceStr, stockStr, desc, imageUrl, existingProduct, progressDialog);
        }
    }

    private void saveProductToFirestore(String name, String code, String category, String priceStr, String stockStr, String desc, String imageUrl, ProductModel existingProduct, ProgressDialog progressDialog) {
        double price = TextUtils.isEmpty(priceStr) ? 0 : Double.parseDouble(priceStr);
        long stock = TextUtils.isEmpty(stockStr) ? 0 : Long.parseLong(stockStr);

        ProductModel product = new ProductModel();
        product.setName(name);
        product.setCode(code);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(desc);
        product.setImageUrl(imageUrl);

        if (existingProduct != null) {
            productRef.document(existingProduct.getId()).set(product).addOnCompleteListener(task -> progressDialog.dismiss());
        } else {
            productRef.add(product).addOnCompleteListener(task -> progressDialog.dismiss());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint(getString(R.string.search_products_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        return true;
    }

    private void performSearch(String text) {
        Query query = text.isEmpty() ? productRef.orderBy("name", Query.Direction.ASCENDING) :
                productRef.orderBy("name").startAt(text).endAt(text + "\uf8ff");
        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class).build();
        adapter.updateOptions(options);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
