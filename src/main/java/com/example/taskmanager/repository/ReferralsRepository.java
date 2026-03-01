package com.example.taskmanager.repository;

import com.example.taskmanager.model.Referrals;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReferralsRepository extends MongoRepository<Referrals,String> {
    
}
