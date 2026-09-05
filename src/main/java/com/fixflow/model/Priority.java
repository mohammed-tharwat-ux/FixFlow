package com.fixflow.model;

public enum Priority {
    CRITICAL(2), // مهلة ساعتين
    HIGH(8),     // مهلة 8 ساعات
    MEDIUM(24),  // مهلة 24 ساعة
    LOW(72);     // مهلة 72 ساعة

    private final int targetHours;

    Priority(int targetHours) {
        this.targetHours = targetHours;
    }

    public int getTargetHours() {
        return targetHours;
    }
}