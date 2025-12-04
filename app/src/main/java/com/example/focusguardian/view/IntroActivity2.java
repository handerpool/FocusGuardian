package com.example.focusguardian.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class IntroActivity2 extends AppCompatActivity {

    private Button btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6;
    private Button btnContinue2;
    private Button selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnOption5 = findViewById(R.id.btnOption5);
        btnOption6 = findViewById(R.id.btnOption6);
        btnContinue2 = findViewById(R.id.btnContinue2);

        setupOptionButtons();

        btnContinue2.setOnClickListener(v -> {
            // Save screen time preference if needed
            String screenTime = "3-4 hours"; // default
            if (selectedButton == btnOption1) screenTime = "under 1 hour";
            else if (selectedButton == btnOption2) screenTime = "1-3 hours";
            else if (selectedButton == btnOption3) screenTime = "3-4 hours";
            else if (selectedButton == btnOption4) screenTime = "4-5 hours";
            else if (selectedButton == btnOption5) screenTime = "5-7 hours";
            else if (selectedButton == btnOption6) screenTime = "7+ hours";

            SharedPreferences prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
            prefs.edit().putString("screen_time", screenTime).apply();

            Intent intent = new Intent(IntroActivity2.this, IntroActivity3.class);
            startActivity(intent);
        });
    }

    private void setupOptionButtons() {
        View.OnClickListener optionClickListener = v -> {
            if (selectedButton != null) {
                selectedButton.setBackgroundResource(R.drawable.bg_button_secondary);
                selectedButton.setTextColor(Color.WHITE);
            }

            selectedButton = (Button) v;
            selectedButton.setBackgroundResource(R.drawable.bg_button_primary);
            selectedButton.setTextColor(Color.BLACK);

            btnContinue2.setEnabled(true);
            btnContinue2.setBackgroundResource(R.drawable.bg_button_primary);
            btnContinue2.setTextColor(Color.BLACK);
            btnContinue2.setAlpha(1f);
        };

        btnOption1.setOnClickListener(optionClickListener);
        btnOption2.setOnClickListener(optionClickListener);
        btnOption3.setOnClickListener(optionClickListener);
        btnOption4.setOnClickListener(optionClickListener);
        btnOption5.setOnClickListener(optionClickListener);
        btnOption6.setOnClickListener(optionClickListener);
    }
}