package com.example.opensearch.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndex {

    private final Map<String, Set<String>> index = new HashMap<>();
    private static final String INDEX_FILE = "repository/index_data.txt";

    public void indexDocument(String indexName, String documentId, String message) {
        String[] words = message.toLowerCase().split("\\s+");
        for (String word : words) {
            String key = indexName + ":" + word;
            index.computeIfAbsent(key, k -> new HashSet<>()).add(documentId);
        }
    }

    public Set<String> search(String indexName, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return index.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(indexName + ":"))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toSet());
        }
        return index.getOrDefault(indexName + ":" + keyword.toLowerCase(), Collections.emptySet());
    }


    public void shutdown() {
        saveIndexToFile();
    }

    private void saveIndexToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INDEX_FILE))) {
            for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
                writer.write(entry.getKey() + ":" + String.join(",", entry.getValue()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving index to file: " + e.getMessage());
        }
    }
}