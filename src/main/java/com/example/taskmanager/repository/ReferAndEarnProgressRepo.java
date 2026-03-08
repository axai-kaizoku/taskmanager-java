package com.example.taskmanager.repository;

import com.example.taskmanager.model.ReferAndEarnProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferAndEarnProgressRepo extends MongoRepository<ReferAndEarnProgress,String> {

    /** Returns true if at least one document has the given referralUId */
    boolean existsByReferralUId(String referralUId);

    /** Returns the total number of documents with the specified referralUId */
    long countByReferralUId(String referralUId);

    /**
     * **count by two fields**
     * @param referralUId
     * @param referralSourceId
     * @return
     */
    long countByReferralUIdAndReferralSourceId(String referralUId,
                                               String referralSourceId);

    List<ReferAndEarnProgress> findAllByReferralSourceId(String referralSourceId);
}
