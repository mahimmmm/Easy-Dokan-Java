package com.easydokan.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
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

public class ProductActivity extends AppCompatActivity {

    private ActivityProductBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProductAdapter adapter;
    private CollectionReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (mAuth.getCurrentUser() != null) {
            productRef = db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("products");
            setupRecyclerView(productRef.orderBy("name", Query.Direction.ASCENDING));
        } else {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.fabAddProduct.setOnClickListener(v -> showAddEditProductDialog(null));
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView(Query query) {
        FirestoreRecyclerOptions<ProductModel> options = new FirestoreRecyclerOptions.Builder<ProductModel>()
                .setQuery(query, ProductModel.class).build();
        adapter = new ProductAdapter(options);
        binding.productRecyclerView.setHasFixedSize(true);
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
                        .setMessage("Are you sure you want to delete this product?")
                        .setPositiveButton("Delete", (dialog, which) -> documentSnapshot.getReference().delete())
                        .setNegativeButton("Cancel", null).show();
            }
        });
    }

    private void showAddEditProductDialog(DocumentSnapshot snapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null);
        builder.setView(dialogView);

        final EditText nameEt = dialogView.findViewById(R.id.name_edit_text);
        final EditText categoryEt = dialogView.findViewById(R.id.category_edit_text);
        final EditText unitEt = dialogView.findViewById(R.id.unit_edit_text);
        final EditText priceEt = dialogView.findViewById(R.id.price_edit_text);
        final EditText stockEt = dialogView.findViewById(R.id.stock_edit_text);

        ProductModel existingProduct = null;
        if (snapshot != null) {
            builder.setTitle(R.string.edit_product);
            existingProduct = snapshot.toObject(ProductModel.class);
            existingProduct.setId(snapshot.getId());

            nameEt.setText(existingProduct.getName());
            categoryEt.setText(existingProduct.getCategory());
            unitEt.setText(existingProduct.getUnit());
            priceEt.setText(String.valueOf(existingProduct.getPrice()));
            stockEt.setText(String.valueOf(existingProduct.getStock()));
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
            saveProductToFirestore(name, categoryEt.getText().toString(), unitEt.getText().toString(),
                    priceEt.getText().toString(), stockEt.getText().toString(), finalExistingProduct);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void saveProductToFirestore(String name, String category, String unit, String priceStr, String stockStr, ProductModel existingProduct) {
        double price = TextUtils.isEmpty(priceStr) ? 0 : Double.parseDouble(priceStr);
        long stock = TextUtils.isEmpty(stockStr) ? 0 : Long.parseLong(stockStr);

        ProductModel product = new ProductModel();
        product.setName(name);
        product.setCategory(category);
        product.setUnit(unit);
        product.setPrice(price);
        product.setStock(stock);
        product.setAdded_by(mAuth.getCurrentUser().getUid());
        // last_updated will be set by @ServerTimestamp

        if (existingProduct != null) {
            productRef.document(existingProduct.getId()).set(product);
        } else {
            productRef.add(product);
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
        String searchText = text.toLowerCase().trim();
        Query query;
        if (searchText.isEmpty()) {
            query = productRef.orderBy("name", Query.Direction.ASCENDING);
        } else {
            query = productRef.orderBy("name").startAt(searchText).endAt(searchText + "\uf8ff");
        }
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
