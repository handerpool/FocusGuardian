package com.example.focusguardian.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class LocalStorage {
    private static final String PREFS = "fg_prefs";
    private static final String KEY_BLOCKED = "blocked_set";
    private final SharedPreferences prefs;

    public LocalStorage(Context ctx){
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Set<String> getBlockedSet(){
        return new HashSet<>(prefs.getStringSet(KEY_BLOCKED, new HashSet<String>()));
    }

    public void setBlockedSet(Set<String> set){
        prefs.edit().putStringSet(KEY_BLOCKED, set).apply();
    }
}
