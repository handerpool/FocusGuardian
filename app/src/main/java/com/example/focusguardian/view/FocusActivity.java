package com.example.focusguardian.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focusguardian.R;

public class FocusActivity extends AppCompatActivity {

    TextView tvMsg;
    Button btnReturn, btnEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        tvMsg = findViewById(R.id.tvAlertMsg);
        btnReturn = findViewById(R.id.btnReturn);
        btnEnd = findViewById(R.id.btnEndSession);

        String pkg = getIntent().getStringExtra("blocked_pkg");
        tvMsg.setText("Blocked app opened: " + (pkg != null ? pkg : "a blocked app"));

        btnReturn.setOnClickListener(v -> finish());

        btnEnd.setOnClickListener(v -> {
            startActivity(new Intent(this, ProofUploadActivity.class));
            finish();
        });
    }

}
