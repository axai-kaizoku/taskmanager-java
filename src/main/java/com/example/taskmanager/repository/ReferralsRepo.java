package com.example.taskmanager.repository;

import com.example.taskmanager.model.Referrals;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralsRepo extends MongoRepository<Referrals,String> {
    Referrals findByUserId(String userId);
    Integer countByUserId(String userId);
    Referrals findByReferralUserId(String referralUserId);
}
