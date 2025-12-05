package com.example.focusguardian.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class IntroActivity4 extends AppCompatActivity {

    private LinearLayout btnContinue4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro4);

        btnContinue4 = findViewById(R.id.btnContinue4);

        btnContinue4.setOnClickListener(v -> {
            // Mark onboarding as complete
            SharedPreferences prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("onboarding_complete", true).apply();

            // Go to main screen
            Intent intent = new Intent(IntroActivity4.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}