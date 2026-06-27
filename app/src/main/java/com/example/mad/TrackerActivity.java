package com.example.mad;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TrackerActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "TrackerActivity";
    private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 100;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView tvSteps, tvDistance;
    private Button btnBack;

    private int initialSteps = -1;
    private int sessionSteps = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        tvSteps = findViewById(R.id.tvSteps);
        tvDistance = findViewById(R.id.tvDistance);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
            Toast.makeText(this, "Step Counter sensor not available on this device", Toast.LENGTH_LONG).show();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            registerStepCounter();
        } else {
            requestPermission();
        }
    }

    private void registerStepCounter() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveStepData(); // Save data when the user leaves the activity
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            if (initialSteps == -1) {
                initialSteps = totalSteps;
            }

            sessionSteps = totalSteps - initialSteps;
            tvSteps.setText(String.valueOf(sessionSteps));

            double distanceKm = (sessionSteps * 0.76) / 1000;
            tvDistance.setText(String.format("Distance: %.2f km", distanceKm));
        }
    }

    private void saveStepData() {
        if (sessionSteps <= 0) {
            // No new steps to save
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return; // Can't save if not logged in
        }

        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String docId = currentUser.getUid() + "_" + dateString;
        DocumentReference dailyStepsRef = db.collection("daily_steps").document(docId);

        // Use a transaction or a batched write to be safer, but for this use case, two separate calls are okay.
        // First, ensure the document exists with basic info.
        Map<String, Object> dailyData = new HashMap<>();
        dailyData.put("userId", currentUser.getUid());
        dailyData.put("date", dateString);
        dailyStepsRef.set(dailyData, SetOptions.merge());

        // Second, increment the step count.
        dailyStepsRef.update("steps", FieldValue.increment(sessionSteps))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully saved " + sessionSteps + " steps.");
                    // Reset session steps after saving to prevent double counting if the user comes back.
                    initialSteps = initialSteps + sessionSteps; 
                    sessionSteps = 0;
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error saving steps", e));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                ACTIVITY_RECOGNITION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerStepCounter();
            } else {
                Toast.makeText(this, "Permission denied. Step counter will not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
