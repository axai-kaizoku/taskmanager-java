package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import io.jsonwebtoken.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthCheck")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<?> healthCheck() throws IOException {
        var apiResponse = new ApiResponse<>();
        apiResponse.setData("UP");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
