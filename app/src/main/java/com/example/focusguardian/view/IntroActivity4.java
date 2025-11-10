package com.example.focusguardian.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focusguardian.R;

public class IntroActivity4 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro4);

        Button btnContinue = findViewById(R.id.btnContinue4);
        btnContinue.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("first_launch", false).apply();
            startActivity(new Intent(this, SelectAppsActivity.class));
            finish();
        });
    }
}
