package com.example.opensearch.utils;

import java.util.HashSet;
import java.util.Set;

public class ValidationUtil {
    private static final Set<String> indexSet = new HashSet<>();

    public static boolean indexExists(String indexName) {
        return indexSet.contains(indexName);
    }

    public static void addIndex(String indexName) {
        indexSet.add(indexName);
    }
}