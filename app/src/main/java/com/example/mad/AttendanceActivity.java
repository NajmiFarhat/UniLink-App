package com.example.mad;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";
    private Button btnScan, btnBack;
    private TextView tvResult, tvStatus, tvClass, tvRoom, tvInstructor, tvScanPrompt;
    private ImageView ivAttendanceIcon;
    private LinearLayout detailsLayout;
    private String studentId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        studentId = getIntent().getStringExtra("STUDENT_ID");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnScan = findViewById(R.id.btnScan);
        btnBack = findViewById(R.id.btnBack);
        tvResult = findViewById(R.id.tvResult);
        tvStatus = findViewById(R.id.tvStatus);
        tvClass = findViewById(R.id.tvClass);
        tvRoom = findViewById(R.id.tvRoom);
        tvInstructor = findViewById(R.id.tvInstructor);
        tvScanPrompt = findViewById(R.id.tvScanPrompt);
        ivAttendanceIcon = findViewById(R.id.ivAttendanceIcon);
        detailsLayout = findViewById(R.id.detailsLayout);

        btnScan.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Scan Campus QR Code");
            integrator.setBeepEnabled(true);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContent = result.getContents();
            processQrContent(qrContent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQrContent(String content) {
        try {
            JSONObject response = new JSONObject(content);
            String status = response.getString("status");
            String className = response.getString("class");
            String room = response.getString("room");
            String instructor = response.getString("instructor");

            // Update UI
            tvStatus.setText("Status: " + status);
            tvClass.setText("Class: " + className);
            tvRoom.setText("Room: " + room);
            tvInstructor.setText("Instructor: " + instructor);

            // Hide scan prompt and button, show details
            tvScanPrompt.setVisibility(View.GONE);
            btnScan.setVisibility(View.GONE);
            detailsLayout.setVisibility(View.VISIBLE);
            ivAttendanceIcon.setVisibility(View.VISIBLE);

            // Save the detailed data to Firestore
            saveAttendance(status, className, room, instructor);

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
            Toast.makeText(AttendanceActivity.this, "Invalid QR Code Data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAttendance(String status, String className, String room, String instructor) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to record attendance.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> attendanceRecord = new HashMap<>();
        attendanceRecord.put("userId", currentUser.getUid());
        attendanceRecord.put("studentId", studentId);
        attendanceRecord.put("status", status);
        attendanceRecord.put("class", className);
        attendanceRecord.put("room", room);
        attendanceRecord.put("instructor", instructor);
        attendanceRecord.put("timestamp", FieldValue.serverTimestamp());

        db.collection("attendance")
                .add(attendanceRecord)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }
}
