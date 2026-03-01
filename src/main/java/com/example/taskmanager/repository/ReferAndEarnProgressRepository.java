package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferAndEarnProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReferAndEarnProgressRepository extends MongoRepository<ReferAndEarnProgress,String> {
    
}
