package com.example.focusguardian.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class IntroActivity3 extends AppCompatActivity {

    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private Button btnOption5, btnOption6, btnOption7, btnOption8;
    private Button btnContinue3;
    private Button selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro3);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnOption5 = findViewById(R.id.btnOption5);
        btnOption6 = findViewById(R.id.btnOption6);
        btnOption7 = findViewById(R.id.btnOption7);
        btnOption8 = findViewById(R.id.btnOption8);
        btnContinue3 = findViewById(R.id.btnContinue3);

        setupOptionButtons();

        btnContinue3.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity3.this, IntroActivity4.class);
            startActivity(intent);
        });
    }

    private void setupOptionButtons() {
        View.OnClickListener optionClickListener = v -> {
            if (selectedButton != null) {
                selectedButton.setBackgroundResource(R.drawable.bg_option_button);
                selectedButton.setTextColor(Color.WHITE);
            }

            selectedButton = (Button) v;
            selectedButton.setBackgroundResource(R.drawable.bg_button_primary);
            selectedButton.setTextColor(Color.BLACK);

            btnContinue3.setEnabled(true);
            btnContinue3.setBackgroundResource(R.drawable.bg_button_gradient_primary);
            btnContinue3.setTextColor(Color.BLACK);
            btnContinue3.setAlpha(1f);
        };

        btnOption1.setOnClickListener(optionClickListener);
        btnOption2.setOnClickListener(optionClickListener);
        btnOption3.setOnClickListener(optionClickListener);
        btnOption4.setOnClickListener(optionClickListener);
        btnOption5.setOnClickListener(optionClickListener);
        btnOption6.setOnClickListener(optionClickListener);
        btnOption7.setOnClickListener(optionClickListener);
        btnOption8.setOnClickListener(optionClickListener);
    }
}