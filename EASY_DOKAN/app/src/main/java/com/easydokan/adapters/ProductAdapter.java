package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.ProductModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Locale;
import java.text.NumberFormat;

public class ProductAdapter extends FirestoreRecyclerAdapter<ProductModel, ProductAdapter.ProductViewHolder> {

    private OnItemClickListener listener;

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<ProductModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull ProductModel model) {
        holder.productName.setText(model.getName());
        holder.productPrice.setText(formatCurrency(model.getPrice()));

        String stockText = String.format(Locale.getDefault(), "%d %s in stock", model.getStock(), model.getUnit());
        holder.stockStatusChip.setText(stockText);

        holder.optionsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.item_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (listener == null) return false;
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return false;

                if (item.getItemId() == R.id.action_edit) {
                    listener.onEditClick(getSnapshots().getSnapshot(currentPosition));
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    listener.onDeleteClick(getSnapshots().getSnapshot(currentPosition));
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
        TextView productName, productPrice;
        Chip stockStatusChip;
        ImageButton optionsMenu;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            stockStatusChip = itemView.findViewById(R.id.stock_status_chip);
            optionsMenu = itemView.findViewById(R.id.options_menu);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position));
                }
            });
        }
    }

    private String formatCurrency(double amount) {
        try {
            return NumberFormat.getCurrencyInstance(new Locale("bn", "BD")).format(amount);
        } catch (Exception e) {
            return NumberFormat.getCurrencyInstance().format(amount);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
        void onEditClick(DocumentSnapshot documentSnapshot);
        void onDeleteClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
