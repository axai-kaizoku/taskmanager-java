package com.example.taskmanager.service;

import com.example.taskmanager.dto.HomeFeedResponse;
import com.example.taskmanager.dto.ReferralHomePageCardDTO;
import org.springframework.stereotype.Service;

@Service
public class HomeFeedService {
    public HomeFeedResponse getAllCards(String userId) {
        HomeFeedResponse homeFeedResponse = new HomeFeedResponse();
        ReferralHomePageCardDTO referralHomePageCardDTO = new ReferralHomePageCardDTO();
        referralHomePageCardDTO.setReferralCount(2);
        referralHomePageCardDTO.setReferralRemindCount(0);
        referralHomePageCardDTO.setText("One more referral to go");
        referralHomePageCardDTO.setSilverCoinOrdered(false);
        referralHomePageCardDTO.setSilverCoinDelivered(false);
        homeFeedResponse.setReferralCard(referralHomePageCardDTO);
        return homeFeedResponse;
    }
}
