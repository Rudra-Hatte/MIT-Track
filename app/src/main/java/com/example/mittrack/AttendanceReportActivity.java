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

public class AttendanceReportActivity extends AppCompatActivity {
    private TextView tvReport;
    private Button btnLogout;
    private String studentRollNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        tvReport = findViewById(R.id.tvReport);
        btnLogout = findViewById(R.id.btnLogout);
        studentRollNo = getIntent().getStringExtra("rollNo");

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceReportActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        String report = loadAttendanceReport();
        tvReport.setText(report);
    }

    private String loadAttendanceReport() {
        File file = new File(getFilesDir(), "Attendance.csv");
        if (!file.exists()) {
            return "No attendance records found.";
        }
        StringBuilder builder = new StringBuilder();
        // Header line: we assume the CSV record is either:
        // Format 1 (student): rollNo,username,timestamp
        // Format 2 (admin): subject,rollNo,username,timestamp
        builder.append("Roll No\tUsername\tDate\tTime\tDay\n");

        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                // Check for admin record (4 parts) or student record (3 parts)
                String roll, username, timestampStr;
                if (parts.length >= 4) {
                    // Admin record: subject, rollNo, username, timestamp
                    roll = parts[1];
                    username = parts[2];
                    timestampStr = parts[3];
                } else if (parts.length >= 3) {
                    // Student record: rollNo, username, timestamp
                    roll = parts[0];
                    username = parts[1];
                    timestampStr = parts[2];
                } else {
                    continue;
                }

                // Filter: Only show records for this student roll number
                if (!roll.equals(studentRollNo)) {
                    continue;
                }

                Date date;
                try {
                    date = parser.parse(timestampStr);
                } catch (ParseException e) {
                    builder.append(roll).append("\t")
                            .append(username).append("\t")
                            .append(timestampStr).append("\n");
                    continue;
                }
                String dateFormatted = dateFormat.format(date);
                String timeFormatted = timeFormat.format(date);
                String dayFormatted = dayFormat.format(date);

                builder.append(roll).append("\t")
                        .append(username).append("\t")
                        .append(dateFormatted).append("\t")
                        .append(timeFormatted).append("\t")
                        .append(dayFormatted).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading attendance records.";
        }
        return builder.toString();
    }
}
