package me.jasonhorkles.bsc.utils;

public class Log {
    public enum Color {
        RED("\u001B[31m"), YELLOW("\u001B[33m"), GREEN("\u001B[32m"), RESET("\u001B[0m");

        private final String logColor;

        Color(String logColor) {
            this.logColor = logColor;
        }

        public String getColor() {
            return logColor;
        }
    }
}
