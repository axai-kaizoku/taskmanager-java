package com.example.taskmanager.dao;

import com.example.taskmanager.model.ReferralClaims;
import com.example.taskmanager.repository.ReferralClaimsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReferralClaimsDAO {
    @Autowired
    private ReferralClaimsRepo referralClaimsRepo;

    public long getCountByUserIdAndReferralSourceId(String userId, String referralSourceId) {
        return referralClaimsRepo.countByUserIdAndReferralSourceId(userId,referralSourceId);
    }

    public ReferralClaims save(ReferralClaims referralClaims) {
        return referralClaimsRepo.save(referralClaims);
    }
}
