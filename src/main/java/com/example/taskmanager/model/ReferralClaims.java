package com.example.taskmanager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "referral_claims")
public class ReferralClaims {
    @Id
    private String id;

    private String referrerUId;

    private ReferralRewardType rewardType;

    private String claimRefId;

    private ReferralState state;

    private Integer amount;
}
