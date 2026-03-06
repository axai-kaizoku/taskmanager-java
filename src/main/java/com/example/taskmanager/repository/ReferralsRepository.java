package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralsRepository extends MongoRepository<Referrals,String> {
    ReferralSource getSourceByUserId(String userId);
    Integer getReferralsCountByUserId(String userId);
}
