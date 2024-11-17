package com.example.texttool;

import java.util.List;
import java.util.Random;

public class RandomParagraphGenerator {
    private final List<String> wordPool; // Words to use for generation
    private final Random random;

    public RandomParagraphGenerator(List<String> wordPool) {
        this.wordPool = wordPool;
        this.random = new Random();
    }

    // Method to generate a random paragraph
    public String generateParagraph(int wordCount, double temperature) {
        StringBuilder paragraph = new StringBuilder();
        // Adjust range based on temperature.
        int range = (int) (wordPool.size() * temperature);
        // Generate the random paragraph.
        for (int i = 0; i < wordCount; i++) {
            int index = random.nextInt(range);
            paragraph.append(wordPool.get(index)).append(" ");
        }
        return paragraph.toString().trim();
    }
}
