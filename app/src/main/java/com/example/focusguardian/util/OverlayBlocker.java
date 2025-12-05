package com.example.focusguardian.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.focusguardian.R;

public class OverlayBlocker {
    private static View overlayView;
    private static boolean shown = false;
    private static CountDownTimer countDownTimer;
    private static final String[] MOTIVATIONAL_QUOTES = {
            "You're doing great! Stay strong.",
            "Every minute counts. Keep going!",
            "Focus is a superpower. Use it!",
            "Distractions wait. Success doesn't.",
            "Your future self will thank you.",
            "Small steps lead to big changes.",
            "Stay committed to your goals!",
            "Discipline equals freedom."
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showOverlay(Context ctx, String blockedPkg) {
        if (overlayView != null) {
            hideOverlay(ctx);
        }
        if (shown) return;

        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                PixelFormat.TRANSLUCENT
        );

        // Apply blur effect for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setBlurBehindRadius(25);
        }

        params.gravity = Gravity.CENTER;

        LayoutInflater inflater = LayoutInflater.from(ctx);
        overlayView = inflater.inflate(R.layout.overlay_blocker, null);

        // Block all touches on root view
        overlayView.setOnTouchListener((v, e) -> true);

        // Setup UI elements
        setupTimeRemaining(ctx, overlayView);
        setupMotivationalQuote(overlayView);
        setupButtons(ctx, overlayView);
        
        // Add entrance animation
        animateEntrance(overlayView);

        wm.addView(overlayView, params);
        shown = true;
    }

    private static void setupTimeRemaining(Context ctx, View view) {
        TextView tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        if (tvTimeRemaining == null) return;

        SharedPreferences prefs = ctx.getSharedPreferences("fg_prefs", Context.MODE_PRIVATE);
        long endTime = prefs.getLong("focus_end_time", 0);
        long currentTime = System.currentTimeMillis();
        long remainingTime = endTime - currentTime;

        if (remainingTime > 0) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            
            countDownTimer = new CountDownTimer(remainingTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long minutes = (millisUntilFinished / 1000) / 60;
                    long seconds = (millisUntilFinished / 1000) % 60;
                    tvTimeRemaining.setText(String.format("%02d:%02d", minutes, seconds));
                }

                @Override
                public void onFinish() {
                    tvTimeRemaining.setText("00:00");
                    hideOverlay(ctx);
                }
            };
            countDownTimer.start();
        } else {
            tvTimeRemaining.setText("--:--");
        }
    }

    private static void setupMotivationalQuote(View view) {
        // Find the motivational text view (it's in a LinearLayout with an emoji)
        View parent = view.findViewById(R.id.overlayRoot);
        if (parent != null) {
            // Random quote selection
            int randomIndex = (int) (Math.random() * MOTIVATIONAL_QUOTES.length);
            // The quote is embedded in the layout, we could update it dynamically if needed
        }
    }

    private static void setupButtons(Context ctx, View view) {
        // I Understand button - takes user to home screen
        View btnCloseOverlay = view.findViewById(R.id.btnCloseOverlay);
        if (btnCloseOverlay != null) {
            btnCloseOverlay.setOnClickListener(v -> {
                // Go to home screen
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(homeIntent);
            });
        }
    }

    private static void animateEntrance(View view) {
        // Scale and fade animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.9f, 1.0f,
                0.9f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(300);

        AlphaAnimation fadeAnimation = new AlphaAnimation(0f, 1f);
        fadeAnimation.setDuration(300);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(fadeAnimation);

        view.startAnimation(animationSet);
    }

    public static void hideOverlay(Context ctx) {
        if (!shown || overlayView == null) return;
        
        // Cancel timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        
        try {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(overlayView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        overlayView = null;
        shown = false;
    }

    public static boolean isShown() {
        return shown;
    }
}
