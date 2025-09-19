package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.SaleItem;
import com.easydokan.models.SaleModel;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SalesHistoryAdapter extends RecyclerView.Adapter<SalesHistoryAdapter.SaleViewHolder> {

    private final List<SaleModel> salesList;

    public SalesHistoryAdapter(List<SaleModel> salesList) {
        this.salesList = salesList;
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_history_item, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        SaleModel sale = salesList.get(position);
        holder.bind(sale);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    class SaleViewHolder extends RecyclerView.ViewHolder {
        private final TextView saleDate;
        private final TextView totalAmount;
        private final TextView itemsSummary;
        private final TextView paymentSummary;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            saleDate = itemView.findViewById(R.id.sale_date);
            totalAmount = itemView.findViewById(R.id.sale_total_amount);
            itemsSummary = itemView.findViewById(R.id.sale_items_summary);
            paymentSummary = itemView.findViewById(R.id.sale_payment_summary);
        }

        public void bind(SaleModel sale) {
            if (sale.getCreated_at() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                saleDate.setText(sdf.format(sale.getCreated_at()));
            }
            totalAmount.setText(String.format("Total: %s", formatCurrency(sale.getTotal_amount())));

            if (sale.getItems() != null && !sale.getItems().isEmpty()) {
                String summary = sale.getItems().stream()
                                     .map(SaleItem::getProductName)
                                     .collect(Collectors.joining(", "));
                itemsSummary.setText("Items: " + summary);
            } else {
                itemsSummary.setText("No items found");
            }

            String payment = String.format("Paid: %s, Due: %s",
                                           formatCurrency(sale.getPaid_amount()),
                                           formatCurrency(sale.getDue_amount()));
            paymentSummary.setText(payment);
        }

        private String formatCurrency(double amount) {
            try {
                return NumberFormat.getCurrencyInstance(new Locale("bn", "BD")).format(amount);
            } catch (Exception e) {
                return NumberFormat.getCurrencyInstance().format(amount);
            }
        }
    }
}
