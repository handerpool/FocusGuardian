package com.example.focusguardian.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class IntroActivity1 extends AppCompatActivity {

    private Button btnOption1, btnOption2, btnOption3, btnOption4, btnContinue1;
    private Button selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro1);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnContinue1 = findViewById(R.id.btnContinue1);

        setupOptionButtons();

        btnContinue1.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity1.this, IntroActivity2.class);
            startActivity(intent);
        });
    }

    private void setupOptionButtons() {
        View.OnClickListener optionClickListener = v -> {
            // Deselect previous button
            if (selectedButton != null) {
                selectedButton.setBackgroundResource(R.drawable.bg_button_secondary);
                selectedButton.setTextColor(Color.WHITE);
            }

            // Select new button
            selectedButton = (Button) v;
            selectedButton.setBackgroundResource(R.drawable.bg_button_primary);
            selectedButton.setTextColor(Color.BLACK);

            // Enable continue button
            btnContinue1.setEnabled(true);
            btnContinue1.setBackgroundResource(R.drawable.bg_button_primary);
            btnContinue1.setTextColor(Color.BLACK);
            btnContinue1.setAlpha(1f);
        };

        btnOption1.setOnClickListener(optionClickListener);
        btnOption2.setOnClickListener(optionClickListener);
        btnOption3.setOnClickListener(optionClickListener);
        btnOption4.setOnClickListener(optionClickListener);
    }
}