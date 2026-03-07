package com.example.taskmanager.service;

import com.example.taskmanager.dto.SignupRequest;

public interface SignupService {
    void signUpUser(SignupRequest signUpRequest, String referrerUserId);
}
