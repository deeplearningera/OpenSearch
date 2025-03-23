package com.example.opensearch.service;

import com.example.opensearch.index.InvertedIndex;
import com.example.opensearch.model.LogEntry;
import com.example.opensearch.utils.FileStorageUtil;
import com.example.opensearch.utils.ValidationUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.opensearch.utils.FileStorageUtil.REPOSITORY_FOLDER;

@Service
public class LogService {

    private final InvertedIndex invertedIndex = new InvertedIndex();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public void storeLogAsync(String indexName, String message, String level) {
        executorService.submit(() -> {
            String id = UUID.randomUUID().toString();
            LogEntry logEntry = new LogEntry(id, indexName, message, level, Instant.now().toEpochMilli());

            FileStorageUtil.appendLogToSegment(indexName, logEntry);

            invertedIndex.indexDocument(indexName, id, message);
        });
    }

    public List<LogEntry> searchLogs(String indexName, String keyword, String level, Long from, Long to, int page, int pageSize) {
        Set<String> documentIds = invertedIndex.search(indexName, keyword);
        List<LogEntry> results = FileStorageUtil.getLogsByIds(indexName, documentIds);

        results = results.stream()
                .filter(log -> filterLog(log, level, from, to))
                .sorted(Comparator.comparingLong(LogEntry::getTimestamp).reversed())
                .collect(Collectors.toList());

        int start = Math.min(page * pageSize, results.size());
        int end = Math.min(start + pageSize, results.size());
        return results.subList(start, end);
    }

    public void createIndex(String indexName) {
        File indexFolder = new File(REPOSITORY_FOLDER, indexName);
        if (indexFolder.mkdirs()) {
            ValidationUtil.addIndex(indexName);
        }
    }

    private boolean filterLog(LogEntry log, String level, Long from, Long to) {
        return (level == null || log.getLevel().equalsIgnoreCase(level)) &&
                (from == null || log.getTimestamp() >= from) &&
                (to == null || log.getTimestamp() <= to);
    }

    public void shutdown() {
        executorService.shutdown();
        invertedIndex.shutdown();
    }
}