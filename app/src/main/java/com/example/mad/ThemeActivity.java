package com.example.mad;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ThemeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private LinearLayout themeRoot;
    private TextView tvStatus, tvLux;
    private ImageView ivIcon;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        // Initialize Views
        themeRoot = findViewById(R.id.themeRoot);
        tvStatus = findViewById(R.id.tvThemeStatus);
        tvLux = findViewById(R.id.tvLuxValue);
        ivIcon = findViewById(R.id.ivThemeIcon);
        btnBack = findViewById(R.id.btnBack);

        // Setup Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        // Back button returns to HomeActivity
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float lux = event.values[0];
        tvLux.setText("Light Level: " + lux + " lux");

        if (lux < 100) {
            // DARK MODE (Low light)
            applyThemeSettings("#212121", "Dark Mode Active", Color.WHITE, android.R.drawable.ic_menu_recent_history, "dark");
            ivIcon.setColorFilter(Color.WHITE);
        } else {
            // LIGHT MODE (Bright light)
            applyThemeSettings("#F3E5F5", "Light Mode Active", Color.parseColor("#6A1B9A"), android.R.drawable.ic_menu_day, "light");
            ivIcon.setColorFilter(null);
        }
    }

    /**
     * Helper method to update UI and save theme state to SharedPreferences
     */
    private void applyThemeSettings(String bgColor, String statusText, int textColor, int iconRes, String themeKey) {
        // Update Local UI
        themeRoot.setBackgroundColor(Color.parseColor(bgColor));
        tvStatus.setText(statusText);
        tvStatus.setTextColor(textColor);
        ivIcon.setImageResource(iconRes);

        // Save State for HomeActivity to follow
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        prefs.edit().putString("theme_mode", themeKey).apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is visible
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to save battery when the activity is not in the foreground
        sensorManager.unregisterListener(this);
    }
}
