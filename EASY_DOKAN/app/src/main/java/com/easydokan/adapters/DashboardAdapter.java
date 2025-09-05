package com.easydokan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.easydokan.R;
import com.easydokan.models.DashboardNavItem;
import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    private List<DashboardNavItem> navItems;
    private OnItemClickListener listener;

    public DashboardAdapter(List<DashboardNavItem> navItems) {
        this.navItems = navItems;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_nav_item, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        DashboardNavItem item = navItems.get(position);
        holder.title.setText(item.getTitle());
        holder.icon.setImageResource(item.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
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

    public interface OnItemClickListener {
        void onItemClick(DashboardNavItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
