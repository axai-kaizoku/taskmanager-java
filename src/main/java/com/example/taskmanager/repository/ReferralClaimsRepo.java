package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferralClaims;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralClaimsRepo extends MongoRepository<ReferralClaims, String> {
    long countByUserIdAndReferralSourceId(String userId, String referralSourceId);
}
