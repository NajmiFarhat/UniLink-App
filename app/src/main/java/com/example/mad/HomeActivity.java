package com.example.mad;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private RecyclerView rvFeatures;
    private List<FeatureModel> featureList;
    private ImageView ivLogout, ivProfile, ivHistory;
    private LinearLayout homeMainLayout;
    private TextView tvDisplayId, tvWelcome;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        homeMainLayout = findViewById(R.id.homeMainLayout);
        tvDisplayId = findViewById(R.id.tvDisplayId);
        tvWelcome = findViewById(R.id.tvWelcome);
        ivProfile = findViewById(R.id.ivProfile);
        ivLogout = findViewById(R.id.ivLogout);
        ivHistory = findViewById(R.id.ivHistory);
        rvFeatures = findViewById(R.id.rvFeatures);

        if (currentUser == null) {
            // Not logged in, redirect to LoginActivity
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return; // Stop further execution
        }

        fetchUserData();

        ivProfile.setOnClickListener(v -> showAccountSettingsDialog());

        ivHistory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        ivLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupRecyclerView();
    }

    private void fetchUserData() {
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                studentId = documentSnapshot.getString("studentId");
                tvDisplayId.setText("ID: " + studentId);
            } else {
                Log.d(TAG, "No such document");
            }
        }).addOnFailureListener(e -> Log.d(TAG, "get failed with ", e));
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyGlobalTheme();
    }

    private void applyGlobalTheme() {
        String theme = getSharedPreferences("Settings", MODE_PRIVATE)
                .getString("theme_mode", "light");

        if (theme.equals("dark")) {
            homeMainLayout.setBackgroundColor(Color.parseColor("#212121"));
            tvDisplayId.setTextColor(Color.WHITE);
            if (tvWelcome != null) tvWelcome.setTextColor(Color.WHITE);
        } else {
            homeMainLayout.setBackgroundColor(Color.parseColor("#FDF2F0"));
            tvDisplayId.setTextColor(Color.BLACK);
            if (tvWelcome != null) tvWelcome.setTextColor(Color.BLACK);
        }
    }

    private void showAccountSettingsDialog() {
        String[] options = {"Edit My Email", "Delete My Account Permanently"};
        new AlertDialog.Builder(this)
                .setTitle("Account Settings")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditUserDialog();
                    } else {
                        confirmDeleteUser();
                    }
                }).show();
    }

    private void showEditUserDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("New Email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        final EditText etPass = new EditText(this);
        etPass.setHint("Current Password");
        etPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        new AlertDialog.Builder(this)
                .setTitle("Update Email")
                .setView(layout)
                .setPositiveButton("Save Changes", (d, w) -> {
                    String newEmail = etEmail.getText().toString().trim();
                    String currentPassword = etPass.getText().toString().trim();
                    if (!newEmail.isEmpty() && !currentPassword.isEmpty()) {
                        updateEmail(newEmail, currentPassword);
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void updateEmail(String newEmail, String currentPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "Verification email sent to " + newEmail, Toast.LENGTH_SHORT).show();
                        // Update email in Firestore as well
                        db.collection("users").document(currentUser.getUid()).update("email", newEmail);
                    }
                });
            } else {
                Toast.makeText(HomeActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser() {
        final EditText etPass = new EditText(this);
        etPass.setHint("Current Password");
        etPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This is permanent. Enter your password to confirm.")
                .setView(etPass)
                .setPositiveButton("Delete", (d, w) -> {
                    String password = etPass.getText().toString();
                    deleteUser(password);
                })
                .setNegativeButton("No", null).show();
    }

    private void deleteUser(String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete user from Firestore first
                db.collection("users").document(currentUser.getUid()).delete().addOnSuccessListener(aVoid -> {
                    // Then delete from Auth
                    currentUser.delete().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    });
                });
            } else {
                Toast.makeText(HomeActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        featureList = new ArrayList<>();
        featureList.add(new FeatureModel("Smart Attendance", android.R.drawable.ic_menu_camera,
                ContextCompat.getColor(this, R.color.card_attendance), ContextCompat.getColor(this, R.color.accent_attendance)));
        featureList.add(new FeatureModel("Safety Assistant", android.R.drawable.ic_menu_mylocation,
                ContextCompat.getColor(this, R.color.card_safety), ContextCompat.getColor(this, R.color.accent_safety)));
        featureList.add(new FeatureModel("Step Counter", android.R.drawable.ic_menu_directions,
                ContextCompat.getColor(this, R.color.card_tracker), ContextCompat.getColor(this, R.color.accent_tracker)));
        featureList.add(new FeatureModel("Auto Theme", android.R.drawable.ic_menu_day,
                ContextCompat.getColor(this, R.color.card_theme), ContextCompat.getColor(this, R.color.accent_theme)));

        rvFeatures.setLayoutManager(new GridLayoutManager(this, 2));

        FeatureAdapter adapter = new FeatureAdapter(featureList, position -> {
            switch (position) {
                case 0:
                    Intent intentAt = new Intent(HomeActivity.this, AttendanceActivity.class);
                    intentAt.putExtra("STUDENT_ID", studentId);
                    startActivity(intentAt);
                    break;
                case 1:
                    startActivity(new Intent(this, SafetyActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(this, TrackerActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(this, ThemeActivity.class));
                    break;
            }
        });
        rvFeatures.setAdapter(adapter);
    }
}
