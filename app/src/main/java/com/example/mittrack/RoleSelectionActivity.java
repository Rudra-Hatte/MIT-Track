package com.example.mittrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private Button btnAdmin, btnStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        btnAdmin = findViewById(R.id.btnAdmin);
        btnStudent = findViewById(R.id.btnStudent);

        btnAdmin.setOnClickListener(v -> {
            // Navigate to Admin Login Screen
            startActivity(new Intent(RoleSelectionActivity.this, AdminLoginActivity.class));
        });

        btnStudent.setOnClickListener(v -> {
            // Navigate to Student Login Screen (reuse your existing LoginActivity)
            startActivity(new Intent(RoleSelectionActivity.this, LoginActivity.class));
        });
    }
}
