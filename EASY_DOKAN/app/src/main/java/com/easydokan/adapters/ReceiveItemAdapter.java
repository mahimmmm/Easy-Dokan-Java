package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.ReceiveItem;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class ReceiveItemAdapter extends RecyclerView.Adapter<ReceiveItemAdapter.ReceiveItemViewHolder> {

    private List<ReceiveItem> receiveItems;
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onRemoveClick(int position);
    }

    public ReceiveItemAdapter(List<ReceiveItem> receiveItems, OnItemInteractionListener listener) {
        this.receiveItems = receiveItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReceiveItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_item_row, parent, false);
        return new ReceiveItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveItemViewHolder holder, int position) {
        ReceiveItem currentItem = receiveItems.get(position);
        holder.productName.setText(currentItem.getProductName());
        holder.details.setText(String.format(Locale.getDefault(), "Qty: %d @ ৳ %.2f", currentItem.getQuantity(), currentItem.getUnitPrice()));
        holder.subtotal.setText(String.format(Locale.getDefault(), "৳ %.2f", currentItem.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return receiveItems.size();
    }

    class ReceiveItemViewHolder extends RecyclerView.ViewHolder {
        TextView productName, details, subtotal;
        MaterialButton removeButton;

        public ReceiveItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.receive_item_product_name);
            details = itemView.findViewById(R.id.receive_item_details);
            subtotal = itemView.findViewById(R.id.receive_item_subtotal);
            removeButton = itemView.findViewById(R.id.receive_item_remove_button);

            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemoveClick(position);
                    }
                }
            });
        }
    }
}
