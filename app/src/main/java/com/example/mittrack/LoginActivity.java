package com.example.mittrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etRollNo, etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MITTrackPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Use the updated id: roll_no
        etRollNo = findViewById(R.id.roll_no);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        btnRegister.setOnClickListener(v -> registerUser());
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void registerUser() {
        String rollNo = etRollNo.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(rollNo) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter roll number, username, and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sharedPreferences.contains(rollNo)) {
            Toast.makeText(this, "User with this roll number already exists. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Save as a simple comma-separated string "username,password"
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(rollNo, username + "," + password);
        editor.apply();
        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
    }

    private void loginUser() {
        String rollNo = etRollNo.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(rollNo) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter roll number, username, and password", Toast.LENGTH_SHORT).show();
            return;
        }
        String stored = sharedPreferences.getString(rollNo, null);
        if (stored == null) {
            Toast.makeText(this, "User not found. Please register.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] parts = stored.split(",");
        if (parts.length < 2) {
            Toast.makeText(this, "Stored user data is corrupted.", Toast.LENGTH_SHORT).show();
            return;
        }
        String storedUsername = parts[0];
        String storedPassword = parts[1];
        if (username.equals(storedUsername) && password.equals(storedPassword)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("rollNo", rollNo);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
        }
    }
}
