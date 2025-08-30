package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.SaleModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SalesAdapter extends FirestoreRecyclerAdapter<SaleModel, SalesAdapter.SaleViewHolder> {

    private OnItemClickListener listener;

    public SalesAdapter(@NonNull FirestoreRecyclerOptions<SaleModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SaleViewHolder holder, int position, @NonNull SaleModel model) {
        holder.customerName.setText(model.getCustomerName());
        holder.paymentMode.setText(model.getPaymentMode());
        holder.saleTotal.setText(String.format("$%.2f", model.getTotalAmount()));

        if (model.getSaleDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            holder.saleDate.setText(sdf.format(model.getSaleDate()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getSnapshots().getSnapshot(adapterPosition));
                }
            }
        });
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_item, parent, false);
        return new SaleViewHolder(view);
    }

    class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, saleDate, paymentMode, saleTotal;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            saleDate = itemView.findViewById(R.id.sale_date);
            paymentMode = itemView.findViewById(R.id.payment_mode);
            saleTotal = itemView.findViewById(R.id.sale_total);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
