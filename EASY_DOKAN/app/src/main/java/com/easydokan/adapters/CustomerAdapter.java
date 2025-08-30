package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easydokan.R;
import com.easydokan.models.CustomerModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class CustomerAdapter extends FirestoreRecyclerAdapter<CustomerModel, CustomerAdapter.CustomerViewHolder> {

    private OnItemClickListener listener;

    public CustomerAdapter(@NonNull FirestoreRecyclerOptions<CustomerModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomerViewHolder holder, int position, @NonNull CustomerModel model) {
        holder.customerName.setText(model.getName());
        holder.customerPhone.setText(model.getPhone());

        holder.optionsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.item_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditClick(getSnapshots().getSnapshot(position));
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteClick(getSnapshots().getSnapshot(position));
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView customerName;
        TextView customerPhone;
        ImageView optionsMenu;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            customerPhone = itemView.findViewById(R.id.customer_phone);
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
