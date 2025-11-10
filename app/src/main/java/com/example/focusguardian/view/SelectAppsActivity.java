package com.example.focusguardian.view;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.focusguardian.R;

import java.util.HashSet;
import java.util.Set;

public class SelectAppsActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private Button btnSave, btnCancel;
    private TextView tvSelectedCount;
    private SharedPreferences prefs;

    // Demo package names with display names and category colors
    private static final String[][] DEMO_APPS = new String[][]{
            {"com.facebook.katana", "Facebook", "#4267B2"},
            {"com.instagram.android", "Instagram", "#E1306C"},
            {"com.apple.messages", "Messages", "#34C759"},
            {"com.facebook.orca", "Messenger", "#0084FF"},
            {"com.zhiliaoapp.musically", "TikTok", "#000000"},
            {"com.twitter.android", "X", "#FFFFFF"},
            {"com.whatsapp", "WhatsApp", "#25D366"},
            {"com.discord", "Discord", "#5865F2"},
            {"com.linkedin.android", "LinkedIn", "#0A66C2"}
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

        for (String[] appData : DEMO_APPS) {
            LinearLayout itemLayout = createAppItem(appData, saved.contains(appData[0]));
            listContainer.addView(itemLayout);
        }

        updateSelectedCount();

        btnSave.setOnClickListener(v -> {
            Set<String> set = new HashSet<>();
            for (int i = 0; i < listContainer.getChildCount(); i++) {
                LinearLayout itemLayout = (LinearLayout) listContainer.getChildAt(i);
                // Find the CheckBox - it's the last child in the layout
                CheckBox cb = (CheckBox) itemLayout.getChildAt(2);
                if (cb != null && cb.isChecked()) {
                    set.add((String) cb.getTag());
                }
            }
            prefs.edit().putStringSet("blocked_set", set).apply();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createAppItem(String[] appData, boolean isChecked) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(24, 20, 24, 20);
        container.setBackgroundResource(R.drawable.app_item_bg);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 16);
        container.setLayoutParams(containerParams);

        // Icon circle
        TextView iconView = new TextView(this);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(Color.parseColor(appData[2]));
        iconView.setBackground(iconBg);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextSize(16);
        iconView.setTextColor(appData[2].equals("#FFFFFF") || appData[2].equals("#000000")
                ? Color.parseColor("#808080") : Color.WHITE);
        iconView.setText(appData[1].substring(0, 1));

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
        iconParams.setMargins(0, 0, 20, 0);
        iconView.setLayoutParams(iconParams);

        // App name
        TextView nameView = new TextView(this);
        nameView.setText(appData[1]);
        nameView.setTextSize(16);
        nameView.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        nameView.setLayoutParams(nameParams);

        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setTag(appData[0]); // Store package name
        checkBox.setChecked(isChecked);
        checkBox.setButtonTintList(ContextCompat.getColorStateList(this, R.color.checkbox_tint));
        checkBox.setOnCheckedChangeListener((buttonView, checked) -> updateSelectedCount());

        container.addView(iconView);
        container.addView(nameView);
        container.addView(checkBox);

        return container;
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedCount() {
        int count = 0;
        for (int i = 0; i < listContainer.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) listContainer.getChildAt(i);
            // CheckBox is the last child (index 2)
            CheckBox cb = (CheckBox) itemLayout.getChildAt(2);
            if (cb != null && cb.isChecked()) count++;
        }
        tvSelectedCount.setText(count + " apps selected");
    }
}