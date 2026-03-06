package com.example.taskmanager;

import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.repository.ReferralsRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ReferralsDAO {
    @Autowired
    public ReferralsRepository referralsRepository;

    public ReferralSource getReferralSourceByUserId(String userId) {
        return referralsRepository.getSourceByUserId(userId);
    }

    public Integer getReferralsCountByUserId(String userId) {
        return referralsRepository.getReferralsCountByUserId(userId);
    }
}
