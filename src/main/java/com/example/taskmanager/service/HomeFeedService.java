package com.example.taskmanager.service;

import com.example.taskmanager.dto.HomeFeedResponse;

public interface HomeFeedService {
    HomeFeedResponse getAllCards(String userId);
}
