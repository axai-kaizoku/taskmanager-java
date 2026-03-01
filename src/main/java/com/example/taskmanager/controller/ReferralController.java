package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.ReferralUsersResponse;
import com.example.taskmanager.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/referrals")
@PreAuthorize("isAuthenticated()")
public class ReferralController {
    @Autowired
    public ReferralService referralService;

    @GetMapping("/referralUsers")
    public ResponseEntity<ApiResponse<ReferralUsersResponse>> getReferralUsers() {
        ReferralUsersResponse referralUsersResponse = referralService.getReferralUsersResponse();
        return ResponseEntity.ok(ApiResponse.success("Referral Users",referralUsersResponse));
    }
}
