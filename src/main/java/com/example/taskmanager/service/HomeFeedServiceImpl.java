package com.example.taskmanager.service;

import com.example.taskmanager.dao.ReferAndEarnDAO;
import com.example.taskmanager.dao.ReferralsDAO;
import com.example.taskmanager.dto.HomeFeedResponse;
import com.example.taskmanager.dto.ReferralHomePageCardDTO;
import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeFeedServiceImpl implements HomeFeedService {

    private final ReferralsDAO referralsDAO;
    private final ReferAndEarnDAO referAndEarnDAO;

    @Override
    public HomeFeedResponse getAllCards(String userId) {
        log.info("Generating home feed cards for user: {}", userId);
        
        int totalReferralCount = referralsDAO.getReferralsCountByUserId(userId);
        if (totalReferralCount == 0) {
            log.debug("No referrals found for user: {}", userId);
            return createEmptyHomeFeedResponse();
        }

        List<Referrals> silverCoinReferrals = referralsDAO.getReferralsByUserIdAndSource(userId, ReferralSource.SILVER_COIN);
        
        long transactingReferralsCount = countTransactingReferrals(silverCoinReferrals);
        
        log.debug("User {} has {} silver coin referrals, {} are transacting", 
            userId, silverCoinReferrals.size(), transactingReferralsCount);

        return buildHomeFeedResponse(silverCoinReferrals.size(), (int) transactingReferralsCount);
    }

    private long countTransactingReferrals(List<Referrals> referrals) {
        return referrals.stream()
            .filter(referral -> !referAndEarnDAO.getReferAndEarnProgressBySourceId(referral.getId()).isEmpty())
            .count();
    }

    private HomeFeedResponse buildHomeFeedResponse(int totalSilverCoinReferrals, int transactingCount) {
        HomeFeedResponse homeFeedResponse = new HomeFeedResponse();
        ReferralHomePageCardDTO referralCard = new ReferralHomePageCardDTO();

        referralCard.setReferralRemindCount(totalSilverCoinReferrals - transactingCount);
        referralCard.setReferralCount(transactingCount);
        referralCard.setText("Silver Coin Reward on 3 referrals");
        referralCard.setSilverCoinOrdered(false);
        referralCard.setSilverCoinDelivered(false);

        homeFeedResponse.setReferralCard(referralCard);
        return homeFeedResponse;
    }

    private HomeFeedResponse createEmptyHomeFeedResponse() {
        return new HomeFeedResponse();
    }
}
