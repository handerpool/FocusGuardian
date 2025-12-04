package com.example.focusguardian.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;
import com.example.focusguardian.data.AuthRepository;
import com.example.focusguardian.data.User;

/**
 * Register Activity for new user account creation.
 * Uses Firebase Authentication with local SQLite caching.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvError, tvLogin, btnBack;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authRepository = AuthRepository.getInstance(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvError = findViewById(R.id.tvError);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });

        btnBack.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void attemptRegistration() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(displayName, email, password, confirmPassword)) {
            return;
        }

        // Show loading state
        showLoading(true);
        hideError();

        // Attempt registration
        authRepository.registerUser(email, password, displayName, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, 
                            "Account created successfully! Welcome, " + displayName + "!", 
                            Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(formatErrorMessage(errorMessage));
                });
            }
        });
    }

    private boolean validateInputs(String displayName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(displayName)) {
            showError("Please enter your name");
            etDisplayName.requestFocus();
            return false;
        }

        if (displayName.length() < 2) {
            showError("Name must be at least 2 characters");
            etDisplayName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            showError("Please enter your email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Please enter a password");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setVisibility(show ? View.GONE : View.VISIBLE);
        etDisplayName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private String formatErrorMessage(String errorMessage) {
        if (errorMessage == null) return "An error occurred";
        
        // Make Firebase error messages more user-friendly
        if (errorMessage.contains("email address is already in use")) {
            return "An account with this email already exists";
        } else if (errorMessage.contains("email address is badly formatted")) {
            return "Please enter a valid email address";
        } else if (errorMessage.contains("password is invalid") || errorMessage.contains("weak password")) {
            return "Password is too weak. Use at least 6 characters";
        } else if (errorMessage.contains("network")) {
            return "Network error. Please check your connection";
        }
        
        return errorMessage;
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
