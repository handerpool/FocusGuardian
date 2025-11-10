package com.example.focusguardian.util;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {

    public static void openUsageAccessSettings(Activity activity){
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        activity.startActivity(intent);
    }

    public static void requestCameraStorage(Activity activity, int requestCode){
        String[] perms = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(activity, perms, requestCode);
    }
}
