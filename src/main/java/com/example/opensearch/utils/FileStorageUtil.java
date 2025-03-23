package com.example.opensearch.utils;

import com.example.opensearch.model.LogEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileStorageUtil {
    public static final String REPOSITORY_FOLDER = "repository";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void appendLogToSegment(String indexName, LogEntry logEntry) {
        try {
            Path indexPath = Paths.get(REPOSITORY_FOLDER, indexName);

            Path latestSegment = getLatestSegmentFile(indexPath);
            if (latestSegment == null || Files.size(latestSegment) >= MAX_FILE_SIZE) {
                latestSegment = Paths.get(indexPath.toString(), indexName + "_" + System.currentTimeMillis() + ".json");
                Files.createFile(latestSegment);
            }

            List<LogEntry> logs = readLogsFromFile(latestSegment);
            logs.add(logEntry);
            writeLogsToFile(latestSegment, logs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<LogEntry> getLogsByIds(String indexName, Set<String> documentIds) {
        List<LogEntry> allLogs = new ArrayList<>();
        try {
            Path indexPath = Paths.get(REPOSITORY_FOLDER, indexName);
            if (!Files.exists(indexPath)) return Collections.emptyList();

            Files.list(indexPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> allLogs.addAll(readLogsFromFile(file)));

            return allLogs.stream().filter(log -> documentIds.contains(log.getId())).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static Path getLatestSegmentFile(Path indexPath) throws IOException {
        return Files.list(indexPath)
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparingLong(FileStorageUtil::getFileTimestamp).reversed())
                .findFirst()
                .orElse(null);
    }

    private static long getFileTimestamp(Path file) {
        try {
            String name = file.getFileName().toString();
            return Long.parseLong(name.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static List<LogEntry> readLogsFromFile(Path filePath) {
        try {
            if (!Files.exists(filePath)) return new ArrayList<>();
            return objectMapper.readValue(Files.readAllBytes(filePath), new TypeReference<List<LogEntry>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void writeLogsToFile(Path filePath, List<LogEntry> logs) {
        try {
            Files.write(filePath, objectMapper.writeValueAsBytes(logs), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}