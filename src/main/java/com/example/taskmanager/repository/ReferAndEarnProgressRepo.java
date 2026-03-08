package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferAndEarnProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferAndEarnProgressRepo extends MongoRepository<ReferAndEarnProgress,String> {
    boolean existsByReferralUId(String referralUId);
}
