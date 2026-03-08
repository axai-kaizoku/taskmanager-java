package com.example.taskmanager.dao;

import com.example.taskmanager.model.ReferAndEarnProgress;
import com.example.taskmanager.repository.ReferAndEarnProgressRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReferAndEarnDAO {
    @Autowired
    private ReferAndEarnProgressRepo referAndEarnProgressRepo;

    public boolean isReferAndEarnProgressEntryExists(String userId) {
        return referAndEarnProgressRepo.existsByReferralUId(userId);
    }

    public long getReferAndEarnProgressCount(String referralUId) {
        return referAndEarnProgressRepo.countByReferralUId(referralUId);
    }

    public long getReferAndEarnProgressCountByReferralUIdAndSourceId(String referralUId, String referralSourceId) {
        return referAndEarnProgressRepo.countByReferralUIdAndReferralSourceId(referralUId,referralSourceId);
    }

    public List<ReferAndEarnProgress> getReferAndEarnProgressBySourceId(String sourceId) {
        return referAndEarnProgressRepo.findAllByReferralSourceId(sourceId);
    }

    public ReferAndEarnProgress save(ReferAndEarnProgress referAndEarnProgress) {
        return referAndEarnProgressRepo.save(referAndEarnProgress);
    }
}
