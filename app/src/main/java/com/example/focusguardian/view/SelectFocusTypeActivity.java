package com.example.focusguardian.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class SelectFocusTypeActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioGroup durationGroup;
    private Button btnStartFocus, btnCancel;
    private TextView tvTitle;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_focus_type);

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);

        tvTitle = findViewById(R.id.tvTitle);
        radioGroup = findViewById(R.id.radioGroup);
        durationGroup = findViewById(R.id.radioDuration);
        btnStartFocus = findViewById(R.id.btnStartFocus);
        btnCancel = findViewById(R.id.btnCancel);

        // Restore previously selected radio buttons
        restoreSelections();

        btnStartFocus.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select your focus type", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedDurationId = durationGroup.getCheckedRadioButtonId();
            if (selectedDurationId == -1) {
                Toast.makeText(this, "Please select session duration", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selected = findViewById(selectedId);
            RadioButton selectedDuration = findViewById(selectedDurationId);

            String focusType = selected.getText().toString().toLowerCase();
            int durationMinutes = Integer.parseInt(selectedDuration.getText().toString().replaceAll("\\D", ""));

            // Confirm before starting
            new AlertDialog.Builder(this)
                    .setTitle("Start Focus?")
                    .setMessage("Start a " + durationMinutes + " min " + focusType + " session?")
                    .setPositiveButton("Start", (dialog, which) -> startFocus(focusType, durationMinutes))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void restoreSelections() {
        String savedFocus = prefs.getString("focus_type", "");
        int savedDuration = prefs.getInt("focus_duration", 0);

        if (!savedFocus.isEmpty()) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
                if (rb.getText().toString().equalsIgnoreCase(savedFocus)) {
                    rb.setChecked(true);
                    break;
                }
            }
        }

        if (savedDuration != 0) {
            for (int i = 0; i < durationGroup.getChildCount(); i++) {
                RadioButton rb = (RadioButton) durationGroup.getChildAt(i);
                if (Integer.parseInt(rb.getText().toString().replaceAll("\\D", "")) == savedDuration) {
                    rb.setChecked(true);
                    break;
                }
            }
        }
    }

    private void startFocus(String focusType, int durationMinutes) {
        // Save selections
        prefs.edit()
                .putString("focus_type", focusType)
                .putInt("focus_duration", durationMinutes)
                .apply();

        // Start MainActivity with focus intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("start_focus", true);
        intent.putExtra("focus_type", focusType);
        intent.putExtra("duration_minutes", durationMinutes);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Focus session started on " + focusType + " for " + durationMinutes + " min", Toast.LENGTH_SHORT).show();
    }
}
