package com.example.mad;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etUser, etEmail, etPass;
    private Button btnRegister, btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUser = findViewById(R.id.regUser);
        etEmail = findViewById(R.id.regEmail);
        etPass = findViewById(R.id.regPass);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                String studentId = etUser.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPass.getText().toString().trim();
                registerUser(studentId, email, password);
            }
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean validateInput() {
        String studentId = etUser.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (studentId.isEmpty()) {
            etUser.setError("Student ID is required");
            etUser.requestFocus();
            return false;
        }

        if (!studentId.matches("[a-zA-Z]+\\d+")) {
            etUser.setError("Invalid format (e.g., CB12345)");
            etUser.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPass.setError("Password is required");
            etPass.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPass.setError("Password must be at least 6 characters");
            etPass.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String studentId, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, now save the user data to Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user.getUid(), studentId, email);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String studentId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("studentId", studentId);
        userData.put("email", email);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
                    mAuth.signOut(); // Sign out the user
                    finish(); // Go back to LoginActivity
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document", e);
                    // Optionally, you might want to delete the created user if saving data fails
                    Toast.makeText(RegisterActivity.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
