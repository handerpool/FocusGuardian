package com.example.focusguardian.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;
import com.example.focusguardian.service.AppMonitorService;

public class SelectFocusTypeActivity extends AppCompatActivity {

    private Button btnCoding, btnStudying, btnCooking, btnTraining;
    private Button btn15min, btn25min, btn45min, btn60min, btn90min, btn120min;
    private Button btnStartSession;
    private TextView tvSelectedFocus, tvSelectedDuration;

    private Button selectedFocusButton;
    private Button selectedDurationButton;

    private String selectedFocusType = "coding";
    private int selectedDurationMinutes = 25;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_focus_type);

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);

        // Initialize focus type buttons
        btnCoding = findViewById(R.id.btnCoding);
        btnStudying = findViewById(R.id.btnStudying);
        btnCooking = findViewById(R.id.btnCooking);
        btnTraining = findViewById(R.id.btnTraining);

        // Initialize duration buttons
        btn15min = findViewById(R.id.btn15min);
        btn25min = findViewById(R.id.btn25min);
        btn45min = findViewById(R.id.btn45min);
        btn60min = findViewById(R.id.btn60min);
        btn90min = findViewById(R.id.btn90min);
        btn120min = findViewById(R.id.btn120min);

        // Initialize other views
        btnStartSession = findViewById(R.id.btnStartSession);
        tvSelectedFocus = findViewById(R.id.tvSelectedFocus);
        tvSelectedDuration = findViewById(R.id.tvSelectedDuration);

        // Set default selections
        selectedFocusButton = btnCoding;
        selectedDurationButton = btn25min;
        highlightButton(btnCoding);
        highlightButton(btn25min);

        // Setup click listeners for focus types
        btnCoding.setOnClickListener(v -> selectFocusType(btnCoding, "💻 Coding", "coding"));
        btnStudying.setOnClickListener(v -> selectFocusType(btnStudying, "📚 Studying", "studying"));
        btnCooking.setOnClickListener(v -> selectFocusType(btnCooking, "🍳 Cooking", "cooking"));
        btnTraining.setOnClickListener(v -> selectFocusType(btnTraining, "🏋️ Training", "training"));

        // Setup click listeners for durations
        btn15min.setOnClickListener(v -> selectDuration(btn15min, 15, "15 minutes"));
        btn25min.setOnClickListener(v -> selectDuration(btn25min, 25, "25 minutes"));
        btn45min.setOnClickListener(v -> selectDuration(btn45min, 45, "45 minutes"));
        btn60min.setOnClickListener(v -> selectDuration(btn60min, 60, "1 hour"));
        btn90min.setOnClickListener(v -> selectDuration(btn90min, 90, "1.5 hours"));
        btn120min.setOnClickListener(v -> selectDuration(btn120min, 120, "2 hours"));

        // Start session button
        btnStartSession.setOnClickListener(v -> startFocusSession());
    }

    private void selectFocusType(Button button, String displayText, String focusType) {
        // Deselect previous button
        if (selectedFocusButton != null) {
            selectedFocusButton.setBackgroundResource(R.drawable.bg_button_secondary);
            selectedFocusButton.setTextColor(Color.WHITE);
        }

        // Select new button
        selectedFocusButton = button;
        highlightButton(button);

        // Update display
        selectedFocusType = focusType;
        tvSelectedFocus.setText(displayText);
    }

    private void selectDuration(Button button, int minutes, String displayText) {
        // Deselect previous button
        if (selectedDurationButton != null) {
            selectedDurationButton.setBackgroundResource(R.drawable.bg_button_secondary);
            selectedDurationButton.setTextColor(Color.WHITE);
        }

        // Select new button
        selectedDurationButton = button;
        highlightButton(button);

        // Update display
        selectedDurationMinutes = minutes;
        tvSelectedDuration.setText(displayText);
    }

    private void highlightButton(Button button) {
        button.setBackgroundResource(R.drawable.bg_button_primary);
        button.setTextColor(Color.BLACK);
    }

    private void startFocusSession() {
        // Save focus type to preferences
        prefs.edit().putString("focus_type", selectedFocusType).apply();

        // Start the monitoring service
        Intent serviceIntent = new Intent(this, AppMonitorService.class);
        serviceIntent.putExtra("duration_minutes", selectedDurationMinutes);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Go back to MainActivity (which should update to show "End Focus" button)
        prefs.edit().putBoolean("focus_active", true).apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}