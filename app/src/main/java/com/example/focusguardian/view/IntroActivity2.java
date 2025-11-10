package com.example.focusguardian.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focusguardian.R;

public class IntroActivity2 extends AppCompatActivity {
    private Button selected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro2);

        Button btnContinue = findViewById(R.id.btnContinue2);
        Button[] options = {
                findViewById(R.id.btnOption1),
                findViewById(R.id.btnOption2),
                findViewById(R.id.btnOption3),
                findViewById(R.id.btnOption4),
                findViewById(R.id.btnOption5),
                findViewById(R.id.btnOption6)
        };

        for (Button b : options) {
            b.setOnClickListener(v -> {
                if (selected != null) selected.setBackgroundResource(R.drawable.rounded_button);
                selected = b;
                b.setBackgroundColor(Color.parseColor("#4CAF50"));
                btnContinue.setEnabled(true);
                btnContinue.setTextColor(Color.WHITE);
                btnContinue.setBackgroundColor(Color.parseColor("#4CAF50"));
            });
        }

        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(this, IntroActivity3.class));
            finish();
        });
    }
}
