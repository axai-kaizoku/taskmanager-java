package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralsRepo extends MongoRepository<Referrals,String> {
    Referrals findByUserId(String userId);
    Integer countByUserId(String userId);
    Referrals findByReferralUserId(String referralUserId);
    List<Referrals> findAllByUserIdAndSource(String userId, ReferralSource source);
}
