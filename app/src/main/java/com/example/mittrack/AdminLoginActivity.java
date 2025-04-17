package com.example.mittrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    // Predefined credentials for admin
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private EditText etUsername, etPassword;
    private Button btnAdminLogin;
    private int failedAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String usernameInput = etUsername.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(usernameInput)) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(passwordInput)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (usernameInput.equals(ADMIN_USERNAME) && passwordInput.equals(ADMIN_PASSWORD)) {
            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
            startActivity(intent);
            finish();
        } else {
            failedAttempts++;
            Toast.makeText(this, "Invalid admin credentials (" + failedAttempts + " failed attempts)", Toast.LENGTH_SHORT).show();

            if (failedAttempts >= 3) {
                btnAdminLogin.setEnabled(false);
                Toast.makeText(this, "Too many failed attempts! Try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
