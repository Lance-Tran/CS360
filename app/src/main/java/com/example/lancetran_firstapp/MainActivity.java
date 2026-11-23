package com.example.lancetran_firstapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Link Views from activity_main.xml
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Login Action
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Register Action
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    private void handleLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = dbHelper.checkUserCredentials(username, password);
        if (isValid) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            navigateToDisplayActivity();
        } else {
            Toast.makeText(this, "Invalid credentials. Try again or create an account.", Toast.LENGTH_LONG).show();
        }
    }

    private void handleRegistration() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill out all fields to register.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkUserExists(username)) {
            Toast.makeText(this, "Username already exists. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isRegistered = dbHelper.registerUser(username, password);
        if (isRegistered) {
            Toast.makeText(this, "Account created! Logging in...", Toast.LENGTH_SHORT).show();
            navigateToDisplayActivity();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDisplayActivity() {
        Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
        startActivity(intent);
        finish();
    }
}