package com.easydokan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.ExpenseModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExpenseAdapter extends FirestoreRecyclerAdapter<ExpenseModel, ExpenseAdapter.ExpenseViewHolder> {

    public ExpenseAdapter(@NonNull FirestoreRecyclerOptions<ExpenseModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull ExpenseModel model) {
        holder.expenseTitle.setText(model.getTitle());
        holder.expenseCategory.setText(model.getCategory());
        holder.expenseAmount.setText(String.format("$%.2f", model.getAmount()));

        if (model.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.expenseDate.setText(sdf.format(model.getDate()));
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView expenseTitle, expenseCategory, expenseAmount, expenseDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseTitle = itemView.findViewById(R.id.expense_title);
            expenseCategory = itemView.findViewById(R.id.expense_category);
            expenseAmount = itemView.findViewById(R.id.expense_amount);
            expenseDate = itemView.findViewById(R.id.expense_date);
        }
    }
}
