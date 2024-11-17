package com.example.texttool;

import java.util.*;

public class TextStatistics {
    private final String text;

    public TextStatistics(String text) {
        this.text = text.toLowerCase(); // Normalize text for case-insensitive processing
    }

    // Method to count total words.
    public int getWordCount() {
        return text.split("\\s+").length;
    }

    // Method to count total sentences.
    public int getSentenceCount() {
        return text.split("[.!?]").length;
    }

    // Method to calculate the frequency of unique words
    public Map<String, Integer> getUniqueWordFrequency() {
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = text.split("\\W+"); // Split text into words ignoring special characters
        for (String word : words) {
            if (!word.isEmpty()) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        return wordFrequency;
    }

    // Method to get the top five most frequent words
    public List<Map.Entry<String, Integer>> getTopFiveWords() {
        Map<String, Integer> wordFrequency = getUniqueWordFrequency();
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordFrequency.entrySet());
        // Sort by frequency.
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        return sortedEntries.subList(0, Math.min(5, sortedEntries.size()));
    }
}
