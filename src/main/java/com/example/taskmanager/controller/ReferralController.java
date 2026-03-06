package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.ReferralUsersResponse;
import com.example.taskmanager.security.UserDetailsImpl;
import com.example.taskmanager.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/referrals")
@PreAuthorize("isAuthenticated()")
public class ReferralController {
    @Autowired
    public ReferralService referralService;

    @GetMapping("/referralUsers")
    public ResponseEntity<ApiResponse<ReferralUsersResponse>> getReferralUsers() throws IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        String id = ((UserDetailsImpl) principal).getId();
        ReferralUsersResponse referralUsersResponse = referralService.getReferralUsersResponse();
        return ResponseEntity.ok(ApiResponse.success("Referral Users",referralUsersResponse));
    }
}
