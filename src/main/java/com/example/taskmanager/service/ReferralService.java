package com.example.taskmanager.service;

import com.example.taskmanager.dao.UserDAO;
import com.example.taskmanager.dto.ReferralLevelCard;
import com.example.taskmanager.dto.ReferralSilverCoinRewardDTO;
import com.example.taskmanager.dto.ReferralUsersDTO;
import com.example.taskmanager.dto.ReferralUsersResponse;
import com.example.taskmanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Service
public class ReferralService {

    @Autowired
    private UserDAO userDAO;

    @Value("classpath:payloads/silver_reward_ui_payload.json")
    Resource stateFile;

    public ReferralUsersResponse getReferralUsersResponse() throws IOException {
        // Fetch users from db
        List<User> users = userDAO.getAllUsers();
        byte[] jsonData = Files.readAllBytes(Paths.get(stateFile.getURI()));
        ObjectMapper mapper = new ObjectMapper();

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
        referralSilverCoinRewardDTO.setRemindUsers(userDAO.getAllUsers());
        referralSilverCoinRewardDTO.setUiPayload(mapper.readTree(jsonData));

        // Set ReferralUsersDTO
        ReferralUsersDTO referralUsersDTO = new ReferralUsersDTO();
        referralUsersDTO.setReferralSilverCoinRewardDTO(referralSilverCoinRewardDTO);

        // Set ReferralUsers main response
        ReferralUsersResponse referralUsersResponse = new ReferralUsersResponse();
        referralUsersResponse.setReferralUsers(referralUsersDTO);

        return referralUsersResponse;
    }
}
