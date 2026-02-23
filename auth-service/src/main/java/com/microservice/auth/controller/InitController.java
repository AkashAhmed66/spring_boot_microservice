package com.microservice.auth.controller;

import com.microservice.auth.config.DataInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/init")
@RequiredArgsConstructor
@Slf4j
public class InitController {

    private final DataInitializer dataInitializer;

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> initializeAdminData() {
        log.info("Received request to initialize admin data");
        
        String result = dataInitializer.initializeData();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        
        if (result.contains("already initialized")) {
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "To reset data, manually clear the database and call /init/admin again");
        return ResponseEntity.ok(response);
    }
}
