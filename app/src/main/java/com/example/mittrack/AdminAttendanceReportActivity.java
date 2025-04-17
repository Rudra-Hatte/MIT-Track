package com.example.mittrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class AdminAttendanceReportActivity extends AppCompatActivity {
    private TextView tvReport;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_attendance_report);

        tvReport = findViewById(R.id.tvReport);
        btnLogout = findViewById(R.id.btnLogout);

        // Set up the logout button to return to AdminLoginActivity
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminAttendanceReportActivity.this, AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Retrieve subject filter from Intent extras (if provided)
        String subjectFilter = getIntent().getStringExtra("subject");
        String report = loadAttendanceReport(subjectFilter);
        tvReport.setText(report);
    }

    private String loadAttendanceReport(String subjectFilter) {
        File file = new File(getFilesDir(), "Attendance.csv");
        if (!file.exists()) {
            return "No attendance records found.";
        }
        StringBuilder builder = new StringBuilder();
        // Header line
        builder.append("Subject\tRoll No\tUsername\tDate\tTime\tDay\n");

        // Parse the timestamp using the default Date.toString() format:
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Assuming record format: subject,rollNo,username,timestamp
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String subject = parts[0];
                    String rollNo = parts[1];
                    String username = parts[2];
                    String timestampStr = parts[3];

                    // If a subject filter exists, skip non-matching records
                    if (subjectFilter != null && !subject.equalsIgnoreCase(subjectFilter)) {
                        continue;
                    }

                    Date date;
                    try {
                        date = parser.parse(timestampStr);
                    } catch (ParseException e) {
                        builder.append(subject).append("\t")
                                .append(rollNo).append("\t")
                                .append(username).append("\t")
                                .append(timestampStr).append("\n");
                        continue;
                    }
                    String dateFormatted = dateFormat.format(date);
                    String timeFormatted = timeFormat.format(date);
                    String dayFormatted = dayFormat.format(date);

                    builder.append(subject).append("\t")
                            .append(rollNo).append("\t")
                            .append(username).append("\t")
                            .append(dateFormatted).append("\t")
                            .append(timeFormatted).append("\t")
                            .append(dayFormatted).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading attendance records.";
        }
        return builder.toString();
    }
}
