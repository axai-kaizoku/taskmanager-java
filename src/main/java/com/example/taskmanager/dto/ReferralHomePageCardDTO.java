package com.example.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReferralHomePageCardDTO {
    private Integer referralCount;
    private Integer referralRemindCount;
    private boolean isSilverCoinOrdered;
    private boolean isSilverCoinDelivered;
    private String text;
}
