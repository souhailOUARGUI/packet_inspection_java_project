package com.example.pkt_inspection;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IntrusionDetectionSystem {
    private List<DetectionRule> rules;
    private Map<String, List<PacketInfo>> packetsBySource;
    private Map<String, Integer> connectionAttempts;
    private Map<String, Set<Integer>> uniquePortsMap = new ConcurrentHashMap<>();
    private AlertCallback alertCallback;

    public IntrusionDetectionSystem() {
        this.rules = initializeRules();
        this.packetsBySource = new ConcurrentHashMap<>();
        this.connectionAttempts = new ConcurrentHashMap<>();
    }

    public void setAlertCallback(AlertCallback callback) {
        this.alertCallback = callback;
    }

private List<DetectionRule> initializeRules() {
    List<DetectionRule> rules = new ArrayList<>();
    // Increased thresholds and timeframes for more realistic detection
    rules.add(new DetectionRule("DoS Detection", DetectionRule.RuleType.DOS_ATTACK, 700, 60));  // 1000 packets per minute
    rules.add(new DetectionRule("Port Scanning", DetectionRule.RuleType.PORT_SCAN, 50, 10));    // 50 different ports in 10 seconds
    return rules;
}




    public void analyzePacket(PacketInfo packet) {
        String sourceIP = packet.getSourceIP();

        // Comment out localhost check for testing
        // if (sourceIP.equals("127.0.0.1") || packet.getDestinationIP().equals("127.0.0.1")) {
        //     return;
        // }

        // Ignore common broadcast addresses
        if (sourceIP.endsWith(".255") || sourceIP.endsWith(".0") ||
                packet.getDestinationIP().endsWith(".255") || packet.getDestinationIP().endsWith(".0")) {
            return;
        }

        packetsBySource.computeIfAbsent(sourceIP, k -> new ArrayList<>()).add(packet);

        detectDoSAttack(packet);
        detectPortScanning(packet);

        // Clean old packets
        cleanOldPackets();
    }




    public void detectDoSAttack(PacketInfo packet) {
        String sourceIP = packet.getSourceIP();
        List<PacketInfo> sourcePackets = packetsBySource.get(sourceIP);

        if (sourcePackets == null) return;

        DetectionRule dosRule = rules.stream()
                .filter(r -> r.getType() == DetectionRule.RuleType.DOS_ATTACK)
                .findFirst()
                .orElse(null);

        if (dosRule == null) return;

        long packetsInTimeWindow = sourcePackets.stream()
                .filter(p -> p.getTimestamp().isAfter(
                        LocalDateTime.now().minusSeconds(dosRule.getTimeWindowSeconds())
                ))
                .count();

        if (packetsInTimeWindow > dosRule.getThreshold()) {
            String alert = "DoS Attack detected from IP: " + sourceIP;
            if (alertCallback != null) {
                alertCallback.onAlert(alert);
            }
        }
    }



    public void detectPortScanning(PacketInfo packet) {
        String sourceIP = packet.getSourceIP();
        String destIP = packet.getDestinationIP();
        int destPort = packet.getDestinationPort();

        // Create a unique key for source-destination pair
        String key = sourceIP + "_" + destIP;

        // Get or create the set of unique ports for this connection
        Set<Integer> uniquePorts = uniquePortsMap.computeIfAbsent(key, k -> new HashSet<>());
        uniquePorts.add(destPort);

        DetectionRule portScanRule = rules.stream()
                .filter(r -> r.getType() == DetectionRule.RuleType.PORT_SCAN)
                .findFirst()
                .orElse(null);

        if (portScanRule == null) return;

        // Only alert if we see many unique ports in a short time
        if (uniquePorts.size() > portScanRule.getThreshold()) {
            String alert = String.format("Port Scanning detected from IP: %s (Attempted %d unique ports to %s)",
                    sourceIP, uniquePorts.size(), destIP);
            System.out.println(alert);
            if (alertCallback != null) {
                alertCallback.onAlert(alert);
            }
            // Reset the ports set after alerting
            uniquePorts.clear();
        }
    }

    public boolean isAnomalyDetected(PacketInfo packet) {
        return false; // Implement anomaly detection logic here
    }

    private void cleanOldPackets() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        packetsBySource.values().forEach(packets ->
                packets.removeIf(p -> p.getTimestamp().isBefore(cutoff))
        );
    }
}