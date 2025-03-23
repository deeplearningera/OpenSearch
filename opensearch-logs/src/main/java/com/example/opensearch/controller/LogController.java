package com.example.opensearch.controller;

import com.example.opensearch.model.LogEntry;
import com.example.opensearch.service.LogService;
import com.example.opensearch.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/index/{indexName}")
    public ResponseEntity<String> createIndex(@PathVariable String indexName) {
        if (ValidationUtil.indexExists(indexName)) {
            return ResponseEntity.badRequest().body("Index already exists");
        }
        try {
            logService.createIndex(indexName);
        } catch (Exception e){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create index.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Index created successfully.");
    }

    @PostMapping("/{indexName}")
    public ResponseEntity<String> storeLog(@PathVariable String indexName,
                                           @RequestParam String message,
                                           @RequestParam String level) {
        if (!ValidationUtil.indexExists(indexName)) {
            return ResponseEntity.badRequest().body("Index does not exist");
        }
        logService.storeLogAsync(indexName, message, level);
        return ResponseEntity.ok("Log stored successfully");
    }

    @GetMapping("/{indexName}")
    public ResponseEntity<?> searchLogs(@PathVariable String indexName,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String level,
                                        @RequestParam(required = false) Long from,
                                        @RequestParam(required = false) Long to,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        if (!ValidationUtil.indexExists(indexName)) {
            return ResponseEntity.badRequest().body("Index does not exist");
        }
        List<LogEntry> logs = logService.searchLogs(indexName, keyword, level, from, to, page, pageSize);
        return ResponseEntity.ok(logs);
    }
}