package com.example.opensearch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {
    private String id;
    private String indexName;
    private String message;
    private String level;
    private Long timestamp;

    public LogEntry(String id, String indexName, String message, String level) {
        this.id = id;
        this.indexName = indexName;
        this.message = message;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
    }
}