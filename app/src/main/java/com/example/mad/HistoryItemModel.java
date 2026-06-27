package com.example.mad;

public class HistoryItemModel {
    private String title;
    private String subtitle;

    public HistoryItemModel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
