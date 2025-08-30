package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.ProductModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.util.Locale;

public class ProductSelectionAdapter extends FirestoreRecyclerAdapter<ProductModel, ProductSelectionAdapter.ProductSelectionViewHolder> {

    private OnItemClickListener listener;

    public ProductSelectionAdapter(@NonNull FirestoreRecyclerOptions<ProductModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductSelectionViewHolder holder, int position, @NonNull ProductModel model) {
        holder.productName.setText(model.getName());
        holder.productDetails.setText(String.format(Locale.getDefault(), "Price: $%.2f | Qty: %d", model.getPrice(), model.getQuantity()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // We need to pass the model with its ID
                String id = getSnapshots().getSnapshot(position).getId();
                model.setId(id);
                listener.onProductSelected(model);
            }
        });
    }

    @NonNull
    @Override
    public ProductSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_selection_item, parent, false);
        return new ProductSelectionViewHolder(view);
    }

    class ProductSelectionViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productDetails;

        public ProductSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productDetails = itemView.findViewById(R.id.product_details);
        }
    }

    public interface OnItemClickListener {
        void onProductSelected(ProductModel product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
