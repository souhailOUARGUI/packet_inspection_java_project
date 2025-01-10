package com.example.pkt_inspection;

public class DetectionRule {
    private String name;
    private RuleType type;
    private int threshold;
    private int timeWindowSeconds;

    public enum RuleType {
        DOS_ATTACK,
        PORT_SCAN,
        ANOMALY
    }

    public DetectionRule(String name, RuleType type, int threshold, int timeWindowSeconds) {
        this.name = name;
        this.type = type;
        this.threshold = threshold;
        this.timeWindowSeconds = timeWindowSeconds;
    }

    // Getters
    public String getName() { return name; }
    public RuleType getType() { return type; }
    public int getThreshold() { return threshold; }
    public int getTimeWindowSeconds() { return timeWindowSeconds; }
}
