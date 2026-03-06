package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.HomeFeedResponse;
import com.example.taskmanager.dto.ReferralHomePageCardDTO;
import com.example.taskmanager.model.User;
import com.example.taskmanager.security.UserDetailsImpl;
import com.example.taskmanager.service.HomeFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/homeFeed")
@PreAuthorize("isAuthenticated()")
public class HomeFeedController {
    @Autowired
    public HomeFeedService homeFeedService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeFeedResponse>> getHomeFeedCards() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        String id = ((UserDetailsImpl) principal).getId();
        HomeFeedResponse homeFeedResponse = homeFeedService.getAllCards(id);
        return ResponseEntity.ok(ApiResponse.success("Homefeed",homeFeedResponse));
    }
}

