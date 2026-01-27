package com.fathzer.skycrapper;

import java.util.Arrays;

public class InputDataParser {
    /**
     * Parses a string of skyscraper clues.
     * <p>The input string should contain a sequence of space-separated integers.
     * The number of integers must be a multiple of 4 (one for each side of the grid: up, down, left, right).
     * If N is the number of integers divided by 4, then N represents the grid size (N x N).
     * The clues are expected in the following order: N up clues, N down clues, N left clues, and N right clues.</p>
     * <p>Each clue must be an integer between 0 and N inclusive.</p>
     * 
     * @param input the string containing the space-separated clues
     * @return an {@link InputData} object containing the parsed clues
     * @throws IllegalArgumentException if the input is null, does not contain a multiple of 4 integers,
     * or if any clue is less than 0 or greater than N.
     */
    public InputData parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be empty");
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length % 4 != 0) {
            throw new IllegalArgumentException("The number of clues must be a multiple of 4");
        }
        int totalCount = parts.length;
        int n = totalCount / 4;
        int[] clues = new int[totalCount];
        for (int i = 0; i < totalCount; i++) {
            try {
                clues[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer found in input: " + parts[i], e);
            }
            if (clues[i] < 0 || clues[i] > n) {
                throw new IllegalArgumentException("Clue " + clues[i] + " is out of range [0, " + n + "]");
            }
        }
        
        int[] up = Arrays.copyOfRange(clues, 0, n);
        int[] down = Arrays.copyOfRange(clues, n, 2 * n);
        int[] left = Arrays.copyOfRange(clues, 2 * n, 3 * n);
        int[] right = Arrays.copyOfRange(clues, 3 * n, 4 * n);
        
        return new InputData(up, left, right, down);
    }

}
