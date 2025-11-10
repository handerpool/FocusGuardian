package com.example.focusguardian.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.example.focusguardian.R;

public class OverlayBlocker {
    private static View overlayView;
    private static boolean shown = false;

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
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // block touches
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        LayoutInflater inflater = LayoutInflater.from(ctx);
        overlayView = inflater.inflate(R.layout.overlay_blocker, null);

        // Block all touches completely
        overlayView.setOnTouchListener((v, e) -> true);

        wm.addView(overlayView, params);
        shown = true;
    }

    public static void hideOverlay(Context ctx) {
        if (!shown || overlayView == null) return;
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
