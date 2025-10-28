package com.easydokan.models;

public class SettingsModel {
    private String language;
    private String theme;
    private boolean notifications;
    private boolean backup_enabled;

    public SettingsModel() {
        // Required empty public constructor for Firestore
    }

    // Getters and Setters
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }
    public boolean isBackup_enabled() { return backup_enabled; }
    public void setBackup_enabled(boolean backup_enabled) { this.backup_enabled = backup_enabled; }
}
