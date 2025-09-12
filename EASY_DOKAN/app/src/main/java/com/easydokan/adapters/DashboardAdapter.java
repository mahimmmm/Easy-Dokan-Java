package com.easydokan.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.DashboardNavItem;
import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    private List<DashboardNavItem> navItems;
    private Context context;

    public DashboardAdapter(Context context, List<DashboardNavItem> navItems) {
        this.context = context;
        this.navItems = navItems;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_nav_item, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        DashboardNavItem currentItem = navItems.get(position);
        holder.icon.setImageResource(currentItem.getIconResId());
        holder.title.setText(currentItem.getTitle());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, currentItem.getActivityClass());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return navItems.size();
    }

    class DashboardViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public DashboardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.nav_item_icon);
            title = itemView.findViewById(R.id.nav_item_title);
        }
    }
}
