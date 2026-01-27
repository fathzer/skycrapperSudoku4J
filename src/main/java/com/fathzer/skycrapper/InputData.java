package com.fathzer.skycrapper;

public record InputData(int[] up, int[] left, int[] right, int[] down) {
    public int size() {
        return up.length;
    }
}
