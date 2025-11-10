package com.example.focusguardian.viewmodel;

import android.content.Context;

import com.example.focusguardian.data.LocalStorage;

import java.util.Set;

/**
 * Simple holder for app-level logic. You can expand to LiveData/Observers later.
 */
public class FocusViewModel {
    private final LocalStorage storage;

    public FocusViewModel(Context ctx){
        storage = new LocalStorage(ctx);
    }

    public Set<String> getBlockedSet(){
        return storage.getBlockedSet();
    }

    public void setBlockedSet(Set<String> s){
        storage.setBlockedSet(s);
    }
}
