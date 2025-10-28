package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.ReceiveItem;
import java.util.List;
import java.util.Locale;

public class ReceiveItemAdapter extends RecyclerView.Adapter<ReceiveItemAdapter.ReceiveItemViewHolder> {

    private final List<ReceiveItem> receiveItems;
    private final OnDeleteListener onDeleteListener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public ReceiveItemAdapter(List<ReceiveItem> receiveItems, OnDeleteListener onDeleteListener) {
        this.receiveItems = receiveItems;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ReceiveItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_item_row, parent, false);
        return new ReceiveItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveItemViewHolder holder, int position) {
        ReceiveItem item = receiveItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return receiveItems.size();
    }

    class ReceiveItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView productNameText;
        private final TextView quantityPriceText;
        private final TextView subtotalText;
        private final ImageButton deleteButton;

        public ReceiveItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.product_name_text);
            quantityPriceText = itemView.findViewById(R.id.quantity_price_text);
            subtotalText = itemView.findViewById(R.id.subtotal_text);
            deleteButton = itemView.findViewById(R.id.delete_item_button);

            deleteButton.setOnClickListener(v -> {
                if (onDeleteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteListener.onDelete(position);
                    }
                }
            });
        }

        public void bind(ReceiveItem item) {
            productNameText.setText(item.getProductName());
            quantityPriceText.setText(String.format(Locale.getDefault(), "Qty: %d @ ৳%.2f", item.getQuantity(), item.getUnitPrice()));
            subtotalText.setText(String.format(Locale.getDefault(), "৳%.2f", item.getSubtotal()));
        }
    }
}
