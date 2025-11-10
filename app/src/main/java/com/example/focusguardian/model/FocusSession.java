package com.example.focusguardian.model;

import java.io.Serializable;
import java.util.List;

public class FocusSession implements Serializable {
    public long startedAt;
    public int durationMinutes;
    public int interruptions;
    public List<String> blockedPackages;
    public String proofBase64;

    public FocusSession() {}
}
