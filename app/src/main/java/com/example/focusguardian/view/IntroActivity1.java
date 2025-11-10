package com.example.focusguardian.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.focusguardian.R;

public class IntroActivity1 extends AppCompatActivity {

    private Button selected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro1);

        Button btnContinue = findViewById(R.id.btnContinue1);
        Button[] options = {
                findViewById(R.id.btnOption1),
                findViewById(R.id.btnOption2),
                findViewById(R.id.btnOption3),
                findViewById(R.id.btnOption4)
        };

        for (Button b : options) {
            b.setOnClickListener(v -> {
                if (selected != null) {
                    selected.setBackgroundResource(R.drawable.rounded_button);
                }
                selected = b;
                b.setBackgroundColor(Color.parseColor("#4CAF50")); // highlight selection
                btnContinue.setEnabled(true);
                btnContinue.setTextColor(Color.WHITE);
                btnContinue.setBackgroundColor(Color.parseColor("#4CAF50"));
            });
        }

        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(this, IntroActivity2.class));
            finish();
        });
    }
}
