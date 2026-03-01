package com.example.taskmanager.service;

import com.example.taskmanager.dto.HomeFeedResponse;
import org.springframework.stereotype.Service;

@Service
public class HomeFeedService {
    public HomeFeedResponse getAllCards() {
        return new HomeFeedResponse();
    }
}
