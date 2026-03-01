package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferralClaims;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReferralClaimsRepository extends MongoRepository<ReferralClaims, String> {

}
