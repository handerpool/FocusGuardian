package com.example.focusguardian.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.focusguardian.R;
import com.example.focusguardian.util.OverlayBlocker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class AppMonitorService extends Service {

    private static final String CHANNEL_ID = "focus_guardian_channel";
    private static final long INTERVAL = 2000; // check every 2 seconds

    private UsageStatsManager usageStatsManager;
    private SharedPreferences prefs;
    private Handler handler = new Handler();
    private Runnable monitorTask;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);
        createNotificationChannel();
        startForeground(1, buildNotification());
        Log.d("AppMonitorService", "Foreground service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) return START_STICKY;
        isRunning = true;

        monitorTask = new Runnable() {
            @Override
            public void run() {
                try {
                    String foregroundApp = getForegroundApp();
                    if (foregroundApp != null) {
                        if (isBlocked(foregroundApp)) {
                            if (!OverlayBlocker.isShown()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    OverlayBlocker.showOverlay(getApplicationContext(), foregroundApp);
                                }
                                Log.d("FocusGuardian", "Overlay shown for: " + foregroundApp);
                            }
                        } else {
                            if (OverlayBlocker.isShown()) {
                                OverlayBlocker.hideOverlay(getApplicationContext());
                                Log.d("FocusGuardian", "Overlay hidden");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                handler.postDelayed(this, INTERVAL);
            }
        };

        handler.post(monitorTask);
        return START_STICKY;
    }

    private boolean isBlocked(String pkg) {
        Set<String> blocked = prefs.getStringSet("blocked_set", Collections.emptySet());
        return blocked.contains(pkg);
    }

    private String getForegroundApp() {
        long end = System.currentTimeMillis();
        long begin = end - 5000;

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, begin, end);

        if (stats == null || stats.isEmpty()) return null;

        UsageStats recent = Collections.max(stats, Comparator.comparingLong(UsageStats::getLastTimeUsed));
        return recent.getPackageName();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Guardian Monitoring",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FocusGuardian active")
                .setContentText("Monitoring apps to help you stay focused")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorTask);
        OverlayBlocker.hideOverlay(getApplicationContext());
        isRunning = false;
        Log.d("AppMonitorService", "Service destroyed");
    }
    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AppMonitorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
