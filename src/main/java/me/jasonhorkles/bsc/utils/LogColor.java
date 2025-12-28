package me.jasonhorkles.bsc.utils;

public enum LogColor {
    RED("\u001B[31m"),
    YELLOW("\u001B[33m"),
    GREEN("\u001B[32m"),
    RESET("\u001B[0m");

    private final String logColor;

    LogColor(String logColor) {
        this.logColor = logColor;
    }

    public String get() {
        return logColor;
    }
}
