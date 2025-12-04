package com.example.focusguardian.view;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;
import com.example.focusguardian.service.AppMonitorService;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnSelectApps, btnStartFocus;
    private TextView tvWelcome, tvSelectedApps, tvFocusEmoji, tvFocusStatus, tvStartButtonText;
    private SharedPreferences prefs;
    private boolean isFocusActive = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
        boolean firstLaunch = prefs.getBoolean("first_launch", true);

        if (firstLaunch) {
            startActivity(new Intent(this, IntroActivity1.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Request overlay permission if not granted
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnSelectApps = findViewById(R.id.btnSelectApps);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvSelectedApps = findViewById(R.id.tvSelectedApps);
        tvFocusEmoji = findViewById(R.id.tvFocusEmoji);
        tvFocusStatus = findViewById(R.id.tvFocusStatus);
        tvStartButtonText = findViewById(R.id.tvStartButtonText);
    }

    private void setupClickListeners() {
        btnSelectApps.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectAppsActivity.class);
            startActivity(i);
        });

        btnStartFocus.setOnClickListener(v -> {
            if (!isFocusActive) {
                startFocusSession();
            } else {
                endFocusSession();
            }
        });
    }

    private void startFocusSession() {
        Set<String> blockedApps = prefs.getStringSet("blocked_set", null);
        if (blockedApps == null || blockedApps.isEmpty()) {
            Toast.makeText(this, "Please select apps to block first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasUsageAccess()) {
            Toast.makeText(this, "Please enable Usage Access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this, SelectFocusTypeActivity.class);
        startActivity(intent);
    }

    private void endFocusSession() {
        // Stop the monitoring service
        Intent serviceIntent = new Intent(this, AppMonitorService.class);
        stopService(serviceIntent);

        // Clear the flag
        prefs.edit().putBoolean("focus_active", false).apply();

        isFocusActive = false;
        updateFocusUI(false);

        // Show proof upload screen
        Intent proofIntent = new Intent(this, ProofUploadActivity.class);
        startActivity(proofIntent);
    }

    @SuppressLint("SetTextI18n")
    private void updateFocusUI(boolean isActive) {
        if (isActive) {
            tvWelcome.setText("Focus Mode\nActive 🔥");
            tvFocusEmoji.setText("🔥");
            tvFocusStatus.setText("Stay focused!");
            tvStartButtonText.setText("End Session");
            btnStartFocus.setBackgroundResource(R.drawable.bg_button_secondary);
        } else {
            tvWelcome.setText("Earn your\nscreen time.");
            tvFocusEmoji.setText("💪");
            tvFocusStatus.setText("Tap to Complete");
            tvStartButtonText.setText("Start Focus");
            btnStartFocus.setBackgroundResource(R.drawable.bg_button_primary);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSelectedAppsText();
        checkAndUpdateFocusState(); // This is the CRUCIAL part!
    }

    private void updateSelectedAppsText() {
        Set<String> blockedApps = prefs.getStringSet("blocked_set", null);
        if (blockedApps != null && !blockedApps.isEmpty()) {
            tvSelectedApps.setText(blockedApps.size() + " apps will be blocked");
        } else {
            tvSelectedApps.setText("No apps selected yet");
        }
    }

    // NEW METHOD: Check if a focus session is active
    private void checkAndUpdateFocusState() {
        boolean prefSaysActive = prefs.getBoolean("focus_active", false);
        boolean serviceRunning = AppMonitorService.isServiceRunning(this);

        isFocusActive = prefSaysActive && serviceRunning;

        // Sync preference with reality (in case service crashed)
        if (!serviceRunning && prefSaysActive) {
            prefs.edit().putBoolean("focus_active", false).apply();
        }

        updateFocusUI(isFocusActive);
    }

    private boolean hasUsageAccess() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }
}