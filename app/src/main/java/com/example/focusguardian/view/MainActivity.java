package com.example.focusguardian.view;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;
import com.example.focusguardian.data.LocalStorage;
import com.example.focusguardian.service.AppMonitorService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button btnSelect, btnStart;
    private TextView tvSelectedApps, tvWelcome;
    private LocalStorage storage;
    private UsageStatsManager usageStatsManager;
    private SharedPreferences prefs;
    private boolean isFocusActive = false;
    private String currentFocusType;
    private int currentDuration;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);

        boolean firstLaunch = prefs.getBoolean("first_launch", true);
        if (firstLaunch) {
            startActivity(new Intent(this, IntroActivity1.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore focus session state
        isFocusActive = prefs.getBoolean("focus_active", false);
        currentFocusType = prefs.getString("focus_type", "");
        currentDuration = prefs.getInt("focus_duration", 25);

        storage = new LocalStorage(this);
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        btnSelect = findViewById(R.id.btnSelectApps);
        btnStart = findViewById(R.id.btnStartFocus);
        tvSelectedApps = findViewById(R.id.tvSelectedApps);
        tvWelcome = findViewById(R.id.tvWelcome);

        updateSelectedAppsDisplay();

        // Update UI if focus session was active
        if (isFocusActive) {
            btnStart.setText("End Focus Session");
            btnStart.setBackgroundResource(R.drawable.btn_end_focus);
            tvWelcome.setText("🎯 Focus Mode Active: " + currentFocusType);
        } else {
            btnStart.setText("Start Focus");
            btnStart.setBackgroundResource(R.drawable.btn_primary);
            tvWelcome.setText("Stay focused, stay productive");
        }

        btnSelect.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectAppsActivity.class);
            startActivity(i);
        });

        btnStart.setOnClickListener(v -> {
            if (!isFocusActive) {
                // Launch SelectFocusTypeActivity to choose type & duration
                Intent i = new Intent(this, SelectFocusTypeActivity.class);
                startActivity(i);
            } else {
                endFocusSession(); // Only ends session manually
            }
        });

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSelectedAppsDisplay();
    }

    @SuppressLint("SetTextI18n")
    public void startFocusSession(String focusType, int durationMinutes) {
        // Save the session
        isFocusActive = true;
        currentFocusType = focusType;
        currentDuration = durationMinutes;
        prefs.edit()
                .putBoolean("focus_active", true)
                .putString("focus_type", focusType)
                .putInt("focus_duration", durationMinutes)
                .apply();

        // Check if apps are selected
        Set<String> blockedApps = prefs.getStringSet("blocked_set", null);
        if (blockedApps == null || blockedApps.isEmpty()) {
            Toast.makeText(this, "Please select apps to block first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check usage access
        if (!hasUsageAccess()) {
            Toast.makeText(this, "Please enable Usage Access", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }

        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        // Start AppMonitorService
        Intent i = new Intent(this, AppMonitorService.class);
        i.putExtra("duration_minutes", durationMinutes);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }

        // Update UI
        btnStart.setText("End Focus Session");
        btnStart.setBackgroundResource(R.drawable.btn_end_focus);
        tvWelcome.setText("🎯 Focus Mode Active: " + focusType);
        Toast.makeText(this, "Focus session started! Stay focused! 💪", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    private void endFocusSession() {
        // Stop session
        isFocusActive = false;
        prefs.edit().putBoolean("focus_active", false).apply();

        btnStart.setText("Start Focus");
        btnStart.setBackgroundResource(R.drawable.btn_primary);
        tvWelcome.setText("Stay focused, stay productive");

        // Go to ProofUploadActivity only when user ends the session
        Intent proofIntent = new Intent(this, ProofUploadActivity.class);
        startActivity(proofIntent);
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedAppsDisplay() {
        Set<String> blockedApps = prefs.getStringSet("blocked_set", null);
        if (blockedApps != null && !blockedApps.isEmpty()) {
            tvSelectedApps.setText(blockedApps.size() + " apps selected to block");
        } else {
            tvSelectedApps.setText("No apps selected yet");
        }
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

    // Handle incoming intent from SelectFocusTypeActivity
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getBooleanExtra("start_focus", false)) {
            String focusType = intent.getStringExtra("focus_type");
            int duration = intent.getIntExtra("duration_minutes", 25);

            startFocusSession(focusType, duration);
        }
    }

    private String getForegroundApp() {
        try {
            long end = System.currentTimeMillis();
            long begin = end - 5000;

            if (usageStatsManager == null) {
                usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            }

            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST, begin, end);
            if (stats == null || stats.isEmpty()) return null;

            UsageStats last = Collections.max(stats, Comparator.comparingLong(UsageStats::getLastTimeUsed));
            return last.getPackageName();
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
