package com.easydokan.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.easydokan.R;
import com.easydokan.models.ProductModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;

public class ProductAdapter extends FirestoreRecyclerAdapter<ProductModel, ProductAdapter.ProductViewHolder> {

    private OnItemClickListener listener;

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<ProductModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull ProductModel model) {
        holder.productName.setText(model.getName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "$%.2f", model.getPrice()));

        if (model.getStock() > 0) {
            holder.stockStatusChip.setText(R.string.in_stock);
            holder.stockStatusChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
        } else {
            holder.stockStatusChip.setText(R.string.out_of_stock);
            holder.stockStatusChip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
        }

        if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(model.getImageUrl())
                    .placeholder(R.drawable.ic_products)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_products);
        }

        holder.optionsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.item_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (listener != null) listener.onEditClick(getSnapshots().getSnapshot(position));
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    if (listener != null) listener.onDeleteClick(getSnapshots().getSnapshot(position));
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        Chip stockStatusChip;
        ImageButton optionsMenu;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            stockStatusChip = itemView.findViewById(R.id.stock_status_chip);
            optionsMenu = itemView.findViewById(R.id.options_menu);
        }
    }

    public interface OnItemClickListener {
        void onEditClick(DocumentSnapshot documentSnapshot);
        void onDeleteClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
