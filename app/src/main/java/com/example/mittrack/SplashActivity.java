package com.example.mittrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Ensure you have an activity_splash.xml

        // Use a Handler to delay for SPLASH_DELAY milliseconds, then launch RoleSelectionActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
