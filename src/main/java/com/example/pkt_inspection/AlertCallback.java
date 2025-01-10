package com.example.pkt_inspection;


@FunctionalInterface
public interface AlertCallback {
    void onAlert(String message);
}