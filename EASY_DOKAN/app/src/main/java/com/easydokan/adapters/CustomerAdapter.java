package com.easydokan.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.CustomerModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomerAdapter extends FirestoreRecyclerAdapter<CustomerModel, CustomerAdapter.CustomerViewHolder> {

    private OnItemClickListener listener;

    public CustomerAdapter(@NonNull FirestoreRecyclerOptions<CustomerModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomerViewHolder holder, int position, @NonNull CustomerModel model) {
        holder.customerName.setText(model.getName());
        holder.customerPhone.setText(model.getPhone());

        double totalDue = model.getTotal_due();
        if (totalDue > 0) {
            holder.dueStatus.setText(String.format("Due: %s", formatCurrency(totalDue)));
            holder.dueStatus.setTextColor(Color.RED);
        } else {
            holder.dueStatus.setText(R.string.cleared);
            holder.dueStatus.setTextColor(Color.parseColor("#006400")); // Dark Green
        }

        if (model.getUpdated_at() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.lastTransactionDate.setText(String.format("Last updated: %s", sdf.format(model.getUpdated_at())));
        } else {
            holder.lastTransactionDate.setText("No recent activity");
        }

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

        holder.clickableLayout.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(getSnapshots().getSnapshot(adapterPosition));
            }
        });
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, customerPhone, dueStatus, lastTransactionDate;
        ImageButton optionsMenu;
        ConstraintLayout clickableLayout;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            customerPhone = itemView.findViewById(R.id.customer_phone);
            dueStatus = itemView.findViewById(R.id.due_status);
            lastTransactionDate = itemView.findViewById(R.id.last_transaction_date);
            optionsMenu = itemView.findViewById(R.id.options_menu);
            clickableLayout = itemView.findViewById(R.id.clickable_layout);
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
