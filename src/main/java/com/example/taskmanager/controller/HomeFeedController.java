package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.HomeFeedResponse;
import com.example.taskmanager.dto.ReferralHomePageCardDTO;
import com.example.taskmanager.service.HomeFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/homeFeed")
@PreAuthorize("isAuthenticated()")
public class HomeFeedController {
    @Autowired
    public HomeFeedService homeFeedService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeFeedResponse>> getHomeFeedCards() {
        HomeFeedResponse homeFeedResponse = new HomeFeedResponse();
        ReferralHomePageCardDTO referralHomePageCardDTO = new ReferralHomePageCardDTO();
        referralHomePageCardDTO.setReferralCount(2);
        referralHomePageCardDTO.setText("One more referral to go");
        homeFeedResponse.setReferralCard(referralHomePageCardDTO);
        return ResponseEntity.ok(ApiResponse.success("Homefeed",homeFeedResponse));
    }
}
