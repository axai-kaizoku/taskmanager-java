package com.example.taskmanager.dao;

import com.example.taskmanager.model.ReferAndEarnProgress;
import com.example.taskmanager.repository.ReferAndEarnProgressRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReferAndEarnDAO {
    @Autowired
    private ReferAndEarnProgressRepo referAndEarnProgressRepo;

    public boolean isReferAndEarnProgressEntryExists(String userId) {
        return referAndEarnProgressRepo.existsByReferralUId(userId);
    }

    public ReferAndEarnProgress save(ReferAndEarnProgress referAndEarnProgress) {
        return referAndEarnProgressRepo.save(referAndEarnProgress);
    }
}
