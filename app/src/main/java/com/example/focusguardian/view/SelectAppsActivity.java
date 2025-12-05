package com.example.focusguardian.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.focusguardian.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectAppsActivity extends AppCompatActivity {

    private LinearLayout listContainer, btnSave, btnCancel;
    private TextView tvSelectedCount, tvSelectionHint, btnSelectAll;
    private TextView tabAll, tabSocial, tabGames, tabMedia;
    private EditText etSearch;
    private View btnBack, iconGlow;
    private SharedPreferences prefs;
    private int selectedCount = 0;
    private List<LinearLayout> appItems = new ArrayList<>();
    private boolean allSelected = false;
    private String currentFilter = "all";
    private TextView selectedTab;

    // Extended app list with categories
    private static final String[][] APPS = new String[][]{
            {"📷", "Instagram", "com.instagram.android", "#E4405F", "social"},
            {"🎵", "TikTok", "com.zhiliaoapp.musically", "#000000", "social"},
            {"📘", "Facebook", "com.facebook.katana", "#1877F2", "social"},
            {"▶️", "YouTube", "com.google.android.youtube", "#FF0000", "media"},
            {"👻", "Snapchat", "com.snapchat.android", "#FFFC00", "social"},
            {"🔴", "Reddit", "com.reddit.frontpage", "#FF4500", "social"},
            {"🐦", "Twitter/X", "com.twitter.android", "#1DA1F2", "social"},
            {"💬", "WhatsApp", "com.whatsapp", "#25D366", "social"},
            {"🎮", "Games", "com.games.placeholder", "#9146FF", "games"},
            {"📺", "Netflix", "com.netflix.mediaclient", "#E50914", "media"},
            {"🎧", "Spotify", "com.spotify.music", "#1DB954", "media"},
            {"💼", "LinkedIn", "com.linkedin.android", "#0A66C2", "social"}
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);

        initViews();
        setupListeners();
        loadApps();
    }

    private void initViews() {
        listContainer = findViewById(R.id.listContainer);
        btnSave = findViewById(R.id.btnSaveApps);
        btnCancel = findViewById(R.id.btnCancel);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvSelectionHint = findViewById(R.id.tvSelectionHint);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        iconGlow = findViewById(R.id.iconGlow);
        
        // Category tabs
        tabAll = findViewById(R.id.tabAll);
        tabSocial = findViewById(R.id.tabSocial);
        tabGames = findViewById(R.id.tabGames);
        tabMedia = findViewById(R.id.tabMedia);
        selectedTab = tabAll;

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            Set<String> set = new HashSet<>();
            for (LinearLayout item : appItems) {
                CheckBox cb = item.findViewWithTag("checkbox");
                if (cb != null && cb.isChecked()) {
                    set.add((String) item.getTag());
                }
            }
            prefs.edit().putStringSet("blocked_set", set).apply();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());

        btnSelectAll.setOnClickListener(v -> toggleSelectAll());

        // Setup category tab listeners
        tabAll.setOnClickListener(v -> selectTab(tabAll, "all"));
        tabSocial.setOnClickListener(v -> selectTab(tabSocial, "social"));
        tabGames.setOnClickListener(v -> selectTab(tabGames, "games"));
        tabMedia.setOnClickListener(v -> selectTab(tabMedia, "media"));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadApps() {
        Set<String> saved = prefs.getStringSet("blocked_set", new HashSet<>());

        for (String[] app : APPS) {
            LinearLayout appItem = createAppItem(app[0], app[1], app[2], app[3], saved.contains(app[2]));
            appItem.setTag(app[2]); // Store package name
            listContainer.addView(appItem);
            appItems.add(appItem);
        }

        updateSelectedCount();
        startIconGlowAnimation();
    }

    private void filterApps(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < appItems.size(); i++) {
            LinearLayout item = appItems.get(i);
            String appName = APPS[i][1].toLowerCase();
            String category = APPS[i][4];
            
            boolean matchesSearch = appName.contains(lowerQuery) || query.isEmpty();
            boolean matchesCategory = currentFilter.equals("all") || category.equals(currentFilter);
            
            if (matchesSearch && matchesCategory) {
                item.setVisibility(View.VISIBLE);
            } else {
                item.setVisibility(View.GONE);
            }
        }
    }

    private void selectTab(TextView tab, String filter) {
        // Update current filter
        currentFilter = filter;
        
        // Deselect previous tab
        if (selectedTab != null) {
            selectedTab.setBackgroundResource(R.drawable.bg_duration_button);
            selectedTab.setTextColor(Color.WHITE);
        }
        
        // Select new tab
        selectedTab = tab;
        selectedTab.setBackgroundResource(R.drawable.bg_button_gradient_primary);
        selectedTab.setTextColor(Color.BLACK);
        
        // Apply filter with current search query
        String searchQuery = etSearch.getText().toString();
        filterApps(searchQuery);
    }

    private void toggleSelectAll() {
        allSelected = !allSelected;
        for (LinearLayout item : appItems) {
            CheckBox cb = item.findViewWithTag("checkbox");
            if (cb != null && cb.isChecked() != allSelected) {
                cb.setChecked(allSelected);
            }
        }
        btnSelectAll.setText(allSelected ? "Deselect All" : "Select All");
    }

    @SuppressLint("SetTextI18n")
    private LinearLayout createAppItem(String emoji, String name, String pkg, String colorHex, boolean isChecked) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setPadding(20, 18, 20, 18);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        itemLayout.setLayoutParams(params);

        // Set initial background
        updateItemBackground(itemLayout, isChecked);

        // Icon circle with color
        FrameLayout iconContainer = new FrameLayout(this);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(56, 56);
        iconContainerParams.setMarginEnd(16);
        iconContainer.setLayoutParams(iconContainerParams);

        // Icon background with app color
        View iconBg = new View(this);
        iconBg.setLayoutParams(new FrameLayout.LayoutParams(56, 56));
        GradientDrawable iconDrawable = new GradientDrawable();
        iconDrawable.setShape(GradientDrawable.OVAL);
        try {
            int color = Color.parseColor(colorHex);
            iconDrawable.setColor(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)));
            iconDrawable.setStroke(2, Color.argb(80, Color.red(color), Color.green(color), Color.blue(color)));
        } catch (Exception e) {
            iconDrawable.setColor(0x332C2C2E);
        }
        iconDrawable.setCornerRadius(28);
        iconBg.setBackground(iconDrawable);
        iconContainer.addView(iconBg);

        // Emoji
        TextView iconView = new TextView(this);
        iconView.setText(emoji);
        iconView.setTextSize(24);
        iconView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams emojiParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        iconView.setLayoutParams(emojiParams);
        iconView.setGravity(Gravity.CENTER);
        iconContainer.addView(iconView);

        // App info container
        LinearLayout infoContainer = new LinearLayout(this);
        infoContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        infoContainer.setLayoutParams(infoParams);

        // App name
        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(16);
        nameView.setTextColor(Color.WHITE);
        nameView.setTypeface(null, Typeface.BOLD);
        infoContainer.addView(nameView);

        // Package hint
        TextView hintView = new TextView(this);
        hintView.setText("Tap to " + (isChecked ? "unblock" : "block"));
        hintView.setTextSize(12);
        hintView.setTextColor(0xFF636366);
        hintView.setTag("hint");
        infoContainer.addView(hintView);

        // Custom checkbox
        FrameLayout checkboxContainer = new FrameLayout(this);
        LinearLayout.LayoutParams cbContainerParams = new LinearLayout.LayoutParams(32, 32);
        checkboxContainer.setLayoutParams(cbContainerParams);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setTag("checkbox");
        checkBox.setChecked(isChecked);
        checkBox.setButtonDrawable(null);
        checkBox.setBackground(ResourcesCompat.getDrawable(getResources(),
                isChecked ? R.drawable.bg_checkbox_checked : R.drawable.bg_checkbox_unchecked, null));
        FrameLayout.LayoutParams cbParams = new FrameLayout.LayoutParams(32, 32);
        checkBox.setLayoutParams(cbParams);

        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) selectedCount++;
            else selectedCount--;
            updateSelectedCount();
            updateItemBackground(itemLayout, checked);
            checkBox.setBackground(ResourcesCompat.getDrawable(getResources(),
                    checked ? R.drawable.bg_checkbox_checked : R.drawable.bg_checkbox_unchecked, null));

            // Update hint text
            TextView hint = infoContainer.findViewWithTag("hint");
            if (hint != null) {
                hint.setText("Tap to " + (checked ? "unblock" : "block"));
            }

            // Animate the item
            animateItemSelection(itemLayout, checked);
        });

        checkboxContainer.addView(checkBox);

        itemLayout.addView(iconContainer);
        itemLayout.addView(infoContainer);
        itemLayout.addView(checkboxContainer);

        // Make entire item clickable
        itemLayout.setClickable(true);
        itemLayout.setFocusable(true);
        itemLayout.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));

        // Update initial count
        if (isChecked) selectedCount++;

        return itemLayout;
    }

    private void updateItemBackground(LinearLayout item, boolean isSelected) {
        item.setBackground(ResourcesCompat.getDrawable(getResources(),
                isSelected ? R.drawable.bg_app_item_selected : R.drawable.bg_app_item_enhanced, null));
    }

    private void animateItemSelection(View item, boolean selected) {
        float startScale = selected ? 0.97f : 1.0f;
        float endScale = selected ? 1.0f : 0.97f;

        item.animate()
                .scaleX(startScale)
                .scaleY(startScale)
                .setDuration(50)
                .withEndAction(() -> item.animate()
                        .scaleX(endScale)
                        .scaleY(endScale)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void startIconGlowAnimation() {
        if (iconGlow == null) return;

        ValueAnimator animator = ValueAnimator.ofFloat(0.2f, 0.5f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            iconGlow.setAlpha(value);
        });
        animator.start();
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedCount() {
        tvSelectedCount.setText(selectedCount + " APPS SELECTED");

        if (selectedCount > 0) {
            tvSelectionHint.setText("Great choice! Stay focused 💪");
            tvSelectionHint.setTextColor(0xFFFF9500);
        } else {
            tvSelectionHint.setText("Tap apps below to select");
            tvSelectionHint.setTextColor(0xFF8E8E93);
        }
    }
}