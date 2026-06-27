package com.example.mad;

public class FeatureModel {
    private String title;
    private int iconRes;
    private int bgColor;     // Background color
    private int accentColor; // Icon/Text color

    public FeatureModel(String title, int iconRes, int bgColor, int accentColor) {
        this.title = title;
        this.iconRes = iconRes;
        this.bgColor = bgColor;
        this.accentColor = accentColor;
    }

    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
    public int getBgColor() { return bgColor; }
    public int getAccentColor() { return accentColor; }
}