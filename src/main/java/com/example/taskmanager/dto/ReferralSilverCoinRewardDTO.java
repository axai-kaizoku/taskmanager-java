package com.example.taskmanager.dto;

import com.example.taskmanager.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReferralSilverCoinRewardDTO {
    private List<ReferralLevelCard> referralList;
    private boolean isSilverCoinCollected;
    private boolean isSilverCoinDelivered;
    private List<User> remindUsers;
    private Object uiPayload;
}
