package com.example.taskmanager.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "refer_and_earn_progress")
public class ReferAndEarnProgress {
    @Id
    private String id;

    private String referrerUId;

    private String referralUId;

    private String referralSourceId;

    private Integer amount;

    private ReferralStep step;

    private ReferralState state;
}
