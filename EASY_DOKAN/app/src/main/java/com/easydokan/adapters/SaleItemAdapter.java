package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.SaleItem;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class SaleItemAdapter extends RecyclerView.Adapter<SaleItemAdapter.SaleItemViewHolder> {

    private List<SaleItem> saleItems;
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onDeleteClick(int position);
    }

    public SaleItemAdapter(List<SaleItem> saleItems, OnItemInteractionListener listener) {
        this.saleItems = saleItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SaleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sale_item_row, parent, false);
        return new SaleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleItemViewHolder holder, int position) {
        SaleItem currentItem = saleItems.get(position);
        holder.productName.setText(currentItem.getProductName());
        holder.details.setText(String.format(Locale.getDefault(), "Qty: %d @ ৳ %.2f", currentItem.getQuantity(), currentItem.getPrice()));
        holder.subtotal.setText(String.format(Locale.getDefault(), "৳ %.2f", currentItem.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return saleItems.size();
    }

    class SaleItemViewHolder extends RecyclerView.ViewHolder {
        TextView productName, details, subtotal;
        MaterialButton deleteButton;

        public SaleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.sale_item_product_name);
            details = itemView.findViewById(R.id.sale_item_details);
            subtotal = itemView.findViewById(R.id.sale_item_subtotal);
            deleteButton = itemView.findViewById(R.id.sale_item_delete_button);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}
