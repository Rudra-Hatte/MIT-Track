package com.example.mittrack;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_SELECT = 102;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";

    private Button btnOpenCamera, btnSelectImage, btnRegisterFace, btnViewAttendance;
    private Bitmap capturedImage;
    private String rollNo;     // Roll number passed from LoginActivity
    private String username;   // Username passed from LoginActivity
    private File registeredFaceFile;

    private FaceRecognitionHelper faceHelper;
    private CircleImageView ivProfile;
    private TextView tvUsername, tvRollNo;
    private TextView tvGreeting, tvGreetingUsername, tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Student layout

        // Check and request permissions if not granted (if not granted, request them)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
        }

        // Retrieve roll number and username from the Intent
        rollNo = getIntent().getStringExtra("rollNo");
        username = getIntent().getStringExtra("username");

        // Initialize views
        btnOpenCamera     = findViewById(R.id.btnOpenCamera);
        btnSelectImage    = findViewById(R.id.btnSelectImage);
        btnRegisterFace   = findViewById(R.id.btnRegisterFace);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        ivProfile         = findViewById(R.id.ivProfile);
        tvUsername        = findViewById(R.id.tvUsername);
        tvRollNo          = findViewById(R.id.tvRollNo);
        tvGreeting        = findViewById(R.id.tvGreeting);
        tvGreetingUsername= findViewById(R.id.tvGreetingUsername);
        tvSubtitle        = findViewById(R.id.tvSubtitle);

        // Set account details in the UI
        tvUsername.setText(username);
        tvRollNo.setText("Roll No: " + rollNo);
        tvGreeting.setText("Hii ");
        tvGreetingUsername.setText(username);
        tvSubtitle.setText("Welcome to MIT-TRACK");

        faceHelper = new FaceRecognitionHelper(this);

        // Load previously registered face if it exists
        registeredFaceFile = new File(getFilesDir(), "RegisteredFaces/" + rollNo + ".jpg");
        if (registeredFaceFile.exists()) {
            Bitmap registeredBitmap = ImageUtils.loadBitmapFromFile(registeredFaceFile);
            if (registeredBitmap != null) {
                ivProfile.setImageBitmap(registeredBitmap);
            }
        }

        // Set onClick listeners

        btnOpenCamera.setOnClickListener(v -> {
            // Check permissions again at click time
            if (!hasRequiredPermissions()) {
                ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            } else {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE);
            }
        });

        btnSelectImage.setOnClickListener(v -> {
            if (!hasRequiredPermissions()) {
                ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            } else {
                dispatchSelectPictureIntent();
            }
        });

        btnRegisterFace.setOnClickListener(v -> registerFace());
        btnViewAttendance.setOnClickListener(v -> viewAttendanceReport());
    }

    private String[] getRequiredPermissions() {
        // For API 33+ (Android 13 and above) request READ_MEDIA_IMAGES instead of READ_EXTERNAL_STORAGE.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private boolean hasRequiredPermissions() {
        String[] requiredPermissions = getRequiredPermissions();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "onRequestPermissionsResult: " + allGranted);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void dispatchTakePictureIntent(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        } else {
            Toast.makeText(this, "No Camera App Found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                capturedImage = (Bitmap) extras.get("data");
                Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show();
                ivProfile.setImageBitmap(capturedImage);
            } else if (requestCode == REQUEST_IMAGE_SELECT) {
                try {
                    capturedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                    ivProfile.setImageBitmap(capturedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void registerFace() {
        if (capturedImage == null) {
            Toast.makeText(this, "Please capture or select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        // Save the captured image as the registered face using roll number as identifier
        File faceDir = new File(getFilesDir(), "RegisteredFaces");
        if (!faceDir.exists()) {
            faceDir.mkdirs();
        }
        registeredFaceFile = new File(faceDir, rollNo + ".jpg");
        boolean saved = ImageUtils.saveBitmapToFile(capturedImage, registeredFaceFile);
        if (saved) {
            Toast.makeText(this, "Face registered successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to register face", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewAttendanceReport() {
        Intent intent = new Intent(MainActivity.this, AttendanceReportActivity.class);
        // Pass the student's roll number so that the report can be filtered
        intent.putExtra("rollNo", rollNo);
        startActivity(intent);
    }
}
