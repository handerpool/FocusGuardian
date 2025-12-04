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
            prefs.edit().putBoolean("first_launch", false).apply();

            // Go to select apps
            Intent intent = new Intent(IntroActivity4.this, SelectAppsActivity.class);
            startActivity(intent);

            // Clear back stack so user can't go back to intro
            finishAffinity();
        });
    }
}