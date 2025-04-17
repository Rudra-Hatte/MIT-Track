package com.example.mittrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AdminMainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_SELECT = 102;
    private static final String TAG = "AttendanceDebug";

    private Button btnOpenCamera, btnSelectImage, btnMarkAttendance, btnViewAttendance;
    private Spinner spinnerSubject;
    private Bitmap adminCapturedImage;  // Holds image captured or selected
    private FaceRecognitionHelper faceHelper; // Must support multi-face detection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        spinnerSubject = findViewById(R.id.spinnerSubject);

        // Initialize faceHelper (ensure your class implements getFaceEmbeddings)
        faceHelper = new FaceRecognitionHelper(this);

        // Set up subjects in spinner
        String[] subjects = {"Math", "Physics", "Chemistry", "Biology", "Computer Science"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        btnOpenCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "No Camera App Found!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent selectIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (selectIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(selectIntent, REQUEST_IMAGE_SELECT);
            } else {
                Toast.makeText(this, "No Gallery App Found!", Toast.LENGTH_SHORT).show();
            }
        });

        btnMarkAttendance.setOnClickListener(v -> {
            String subject = spinnerSubject.getSelectedItem().toString();
            if (adminCapturedImage == null) {
                Toast.makeText(this, "Please capture or select an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get all face embeddings from the admin captured image (group photo)
            float[][] adminEmbeddings = faceHelper.getFaceEmbeddings(adminCapturedImage);
            if (adminEmbeddings == null || adminEmbeddings.length == 0) {
                Toast.makeText(this, "No faces detected in the class image. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            // If valid, mark attendance for registered students whose registered face appears in the admin image
            markAttendanceForAllStudents(subject, adminEmbeddings);
        });

        btnViewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminAttendanceReportActivity.class);
            intent.putExtra("subject", spinnerSubject.getSelectedItem().toString());
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                adminCapturedImage = (Bitmap) extras.get("data");
                Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_IMAGE_SELECT) {
                try {
                    adminCapturedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Compare each student's registered face with the detected faces in the admin image
    private void markAttendanceForAllStudents(String subject, float[][] adminEmbeddings) {
        File faceDir = new File(getFilesDir(), "RegisteredFaces");
        if (!faceDir.exists() || !faceDir.isDirectory()) {
            Toast.makeText(this, "No registered students found.", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] studentFiles = faceDir.listFiles();
        if (studentFiles == null || studentFiles.length == 0) {
            Toast.makeText(this, "No registered students found.", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("MITTrackPrefs", MODE_PRIVATE);
        File attendanceFile = new File(getFilesDir(), "Attendance.csv");
        double threshold = 1.2; // Increased threshold for matching
        int countMatches = 0;
        try (FileWriter writer = new FileWriter(attendanceFile, true)) {
            for (File file : studentFiles) {
                String fileName = file.getName(); // e.g., "20.jpg"
                if (!fileName.endsWith(".jpg"))
                    continue;
                String roll = fileName.substring(0, fileName.length() - 4);
                String userData = prefs.getString(roll, null);
                if (userData == null)
                    continue;
                String studentName = userData.split(",")[0];
                Bitmap studentFace = ImageUtils.loadBitmapFromFile(file);
                float[] studentEmbedding = faceHelper.getFaceEmbedding(studentFace);
                if (studentEmbedding == null)
                    continue;
                boolean matchFound = false;
                for (float[] emb : adminEmbeddings) {
                    double distance = calculateEuclideanDistance(studentEmbedding, emb);
                    Log.d(TAG, "Roll " + roll + " distance: " + distance);
                    if (distance < threshold) {
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound) {
                    String record = subject + "," + roll + "," + studentName + "," + new Date().toString() + "\n";
                    writer.append(record);
                    countMatches++;
                }
            }
            writer.flush();
            Toast.makeText(this, "Attendance marked for " + countMatches + " students for subject: " + subject, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error marking attendance", Toast.LENGTH_SHORT).show();
        }
    }

    // Euclidean distance helper
    private double calculateEuclideanDistance(float[] emb1, float[] emb2) {
        double sum = 0;
        for (int i = 0; i < emb1.length; i++) {
            sum += Math.pow(emb1[i] - emb2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
