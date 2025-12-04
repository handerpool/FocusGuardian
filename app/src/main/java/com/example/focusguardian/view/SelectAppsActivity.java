package com.example.focusguardian.view;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.focusguardian.R;

import java.util.HashSet;
import java.util.Set;

public class SelectAppsActivity extends AppCompatActivity {

    private LinearLayout listContainer, btnSave, btnCancel;
    private TextView tvSelectedCount;
    private SharedPreferences prefs;
    private int selectedCount = 0;

    private static final String[][] APPS = new String[][]{
            {"📷", "Instagram", "com.instagram.android", "#E4405F"},
            {"🎵", "TikTok", "com.zhiliaoapp.musically", "#000000"},
            {"📘", "Facebook", "com.facebook.katana", "#1877F2"},
            {"▶️", "YouTube", "com.google.android.youtube", "#FF0000"},
            {"👻", "Snapchat", "com.snapchat.android", "#FFFC00"},
            {"🔴", "Reddit", "com.reddit.frontpage", "#FF4500"}
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        listContainer = findViewById(R.id.listContainer);
        btnSave = findViewById(R.id.btnSaveApps);
        btnCancel = findViewById(R.id.btnCancel);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet("blocked_set", new HashSet<>());

        // Create app items
        for (String[] app : APPS) {
            LinearLayout appItem = createAppItem(app[0], app[1], app[2], app[3], saved.contains(app[2]));
            listContainer.addView(appItem);
        }

        updateSelectedCount();

        btnSave.setOnClickListener(v -> {
            Set<String> set = new HashSet<>();
            for (int i = 0; i < listContainer.getChildCount(); i++) {
                View child = listContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    CheckBox cb = (CheckBox) ((LinearLayout) child).getChildAt(2);
                    if (cb.isChecked()) {
                        set.add((String) cb.getTag());
                    }
                }
            }
            prefs.edit().putStringSet("blocked_set", set).apply();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createAppItem(String emoji, String name, String pkg, String colorHex, boolean isChecked) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setPadding(24, 20, 24, 20);
        itemLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_app_item, null));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        itemLayout.setLayoutParams(params);

        // Icon circle
        TextView iconView = new TextView(this);
        iconView.setText(emoji);
        iconView.setTextSize(32);
        iconView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(64, 64);
        iconParams.setMarginEnd(16);
        iconView.setLayoutParams(iconParams);

        // App name
        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(17);
        nameView.setTextColor(Color.WHITE);
        nameView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        nameView.setLayoutParams(nameParams);

        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setTag(pkg);
        checkBox.setChecked(isChecked);
        checkBox.setButtonDrawable(null);
        checkBox.setBackground(ResourcesCompat.getDrawable(getResources(),
                android.R.drawable.checkbox_on_background, null));
        checkBox.setScaleX(1.3f);
        checkBox.setScaleY(1.3f);

        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) selectedCount++;
            else selectedCount--;
            updateSelectedCount();
        });

        itemLayout.addView(iconView);
        itemLayout.addView(nameView);
        itemLayout.addView(checkBox);

        // Update initial count
        if (isChecked) selectedCount++;

        return itemLayout;
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedCount() {
        tvSelectedCount.setText(selectedCount + " APPS SELECTED");
    }
}