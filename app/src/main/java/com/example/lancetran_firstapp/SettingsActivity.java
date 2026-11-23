package com.example.lancetran_firstapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private Switch switchSmsPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Bind the switch from activity_settings.xml
        switchSmsPermission = findViewById(R.id.switchSmsPermission);

        // Check initial permission status on load
        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        switchSmsPermission.setChecked(hasPermission);

        // Handle user toggling the switch
        switchSmsPermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Check if permission is already granted; if not, request it
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SettingsActivity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                SMS_PERMISSION_CODE);
                    } else {
                        Toast.makeText(SettingsActivity.this, "SMS notifications are enabled.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SettingsActivity.this, "SMS alerts disabled. App functions normally.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchSmsPermission.setChecked(true);
                Toast.makeText(this, "SMS permission granted! Alerts enabled.", Toast.LENGTH_SHORT).show();
            } else {
                // User denied permission: reset toggle and notify graceful fallback
                switchSmsPermission.setChecked(false);
                Toast.makeText(this, "Permission denied. App continues with in-app alerts.", Toast.LENGTH_LONG).show();
            }
        }
    }
}