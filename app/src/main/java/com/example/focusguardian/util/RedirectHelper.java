package com.example.focusguardian.util;

import android.content.Context;
import android.content.Intent;

import com.example.focusguardian.view.FocusActivity;
public class RedirectHelper {
    public static void showFocusOverlay(Context ctx, String packageName){
        Intent i = new Intent(ctx, FocusActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("blocked_pkg", packageName);
        ctx.startActivity(i);
    }
}
