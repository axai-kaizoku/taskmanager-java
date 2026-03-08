package com.example.taskmanager.dao;

import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import com.example.taskmanager.repository.ReferralsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReferralsDAO {
    @Autowired
    public ReferralsRepo referralsRepository;

    public ReferralSource getReferralSourceByUserId(String userId) {
        Referrals referral = referralsRepository.findByUserId(userId);
        return (referral != null) ? referral.getSource() : ReferralSource.NO_REFERRER;
    }

    public Integer getReferralsCountByUserId(String userId) {
        return referralsRepository.countByUserId(userId);
    }

    public Referrals getReferralsByReferralUserId(String referralUserId) {
        return referralsRepository.findByReferralUserId(referralUserId);
    }

    public List<Referrals> getReferralsByUserIdAndSource(String userId, ReferralSource source) {
        return referralsRepository.findAllByUserIdAndSource(userId,source);
    }

    public Referrals getReferralSourceByReferralUserId(String referralUserId) {
        return referralsRepository.findByReferralUserId(referralUserId);
    }

    public Referrals saveReferralsEntry(Referrals referrals) {
        return referralsRepository.save(referrals);
    }
}
