package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.SaleItemModel;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<SaleItemModel> cartItems;
    private OnItemRemoveListener listener;

    public CartAdapter(List<SaleItemModel> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SaleItemModel item = cartItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.quantityText.setText(String.format(Locale.getDefault(), "Qty: %d @ $%.2f", item.getQuantity(), item.getPrice()));
        holder.subtotalText.setText(String.format(Locale.getDefault(), "Subtotal: $%.2f", item.getSubtotal()));

        holder.removeItemButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantityText, subtotalText;
        ImageButton removeItemButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            quantityText = itemView.findViewById(R.id.quantity_text);
            subtotalText = itemView.findViewById(R.id.subtotal_text);
            removeItemButton = itemView.findViewById(R.id.remove_item_button);
        }
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.listener = listener;
    }
}
