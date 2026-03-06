package com.example.taskmanager.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Data
@Document(collection = "referrals")
public class Referrals {
    @Id
    private String id;

    private String userId;

    private String referralUserId;

    private ReferralSource source = ReferralSource.NO_REFERRER;
}
