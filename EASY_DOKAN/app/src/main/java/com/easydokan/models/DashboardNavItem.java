package com.easydokan.models;

public class DashboardNavItem {
    private String title;
    private int iconResId;

    public DashboardNavItem(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}
