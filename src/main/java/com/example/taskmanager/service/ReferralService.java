package com.example.taskmanager.service;

import com.example.taskmanager.dto.ReferralLevelCard;
import com.example.taskmanager.dto.ReferralSilverCoinRewardDTO;
import com.example.taskmanager.dto.ReferralUsersDTO;
import com.example.taskmanager.dto.ReferralUsersResponse;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ReferralService {

    @Autowired
    public UserRepository userRepository;

    public ReferralUsersResponse getReferralUsersResponse() {
        // Fetch users from db
        List<User> users = userRepository.findAll();

        // Set ReferralLevel Card
        ReferralLevelCard referralLevelCard = new ReferralLevelCard();
        referralLevelCard.setUser(users.get(0));
        referralLevelCard.setLevel(0);
        referralLevelCard.setCollected(false);
        List<ReferralLevelCard> singleItemList = Collections.singletonList(referralLevelCard); // convert into list

        // Set ReferralSilverCoinReward
        ReferralSilverCoinRewardDTO referralSilverCoinRewardDTO = new ReferralSilverCoinRewardDTO();
        referralSilverCoinRewardDTO.setReferralList(singleItemList);
        referralSilverCoinRewardDTO.setSilverCoinCollected(false);
        referralSilverCoinRewardDTO.setSilverCoinDelivered(false);
        referralSilverCoinRewardDTO.setRemindUsers(userRepository.findAll());

        // Set ReferralUsersDTO
        ReferralUsersDTO referralUsersDTO = new ReferralUsersDTO();
        referralUsersDTO.setReferralSilverCoinRewardDTO(referralSilverCoinRewardDTO);

        // Set ReferralUsers main response
        ReferralUsersResponse referralUsersResponse = new ReferralUsersResponse();
        referralUsersResponse.setReferralUsers(referralUsersDTO);

        return referralUsersResponse;
    }
}
