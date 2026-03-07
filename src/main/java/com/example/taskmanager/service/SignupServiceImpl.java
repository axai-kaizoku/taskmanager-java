package com.example.taskmanager.service;

import com.example.taskmanager.dao.ReferralsDAO;
import com.example.taskmanager.dao.UserDAO;
import com.example.taskmanager.dto.SignupRequest;
import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import com.example.taskmanager.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final ReferralsDAO referralsDAO;

    private static final String SIGNUP_SERVICE_TAG = "SIGNUP SERVICE: ";

    @Override
    public void signUpUser(SignupRequest signUpRequest, String referrerUserId) {
        validateEmailNotTaken(signUpRequest.getEmail());

        User user = createAndSaveUser(signUpRequest);
        createAndSaveReferralEntry(user.getId(), referrerUserId);
    }

    private void validateEmailNotTaken(String email) {
        if (userDAO.getUserByEmail(email) != null) {
            log.error("{}Email {} is already in use!", SIGNUP_SERVICE_TAG, email);
            throw new RuntimeException("Error: Email is already in use!");
        }
    }

    private User createAndSaveUser(SignupRequest signUpRequest) {
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRoles(determineRoles(signUpRequest.getRoles()));

        return userDAO.saveUser(user);
    }

    private Set<String> determineRoles(Set<String> strRoles) {
        Set<String> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add("ROLE_USER");
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        roles.add("ROLE_ADMIN");
                        break;
                    case "mod":
                        roles.add("ROLE_MODERATOR");
                        break;
                    default:
                        roles.add("ROLE_USER");
                }
            });
        }
        return roles;
    }

    private void createAndSaveReferralEntry(String newUserId, String referrerUserId) {
        Referrals referralsEntry = new Referrals();
        referralsEntry.setReferralUserId(newUserId);

        if (referrerUserId == null || referrerUserId.isEmpty()) {
            referralsEntry.setUserId(null);
            referralsEntry.setSource(ReferralSource.NO_REFERRER);
        } else {
            log.info("{}REFERRER USER ID FOUND: {}", SIGNUP_SERVICE_TAG, referrerUserId);
            referralsEntry.setUserId(referrerUserId);
            referralsEntry.setSource(determineReferralSource(referrerUserId));
        }

        referralsDAO.saveReferralsEntry(referralsEntry);
    }

    private ReferralSource determineReferralSource(String referrerUserId) {
        ReferralSource existingSource = referralsDAO.getReferralSourceByUserId(referrerUserId);
        
        // If the referrer has a source, we might want to propagate it or logic as per original code
        if (existingSource != ReferralSource.NO_REFERRER && existingSource != null) {
            return existingSource;
        }

        int referralCount = referralsDAO.getReferralsCountByUserId(referrerUserId);
        log.debug("{}Referral count for {}: {}", SIGNUP_SERVICE_TAG, referrerUserId, referralCount);

        if (referralCount == 0) {
            return ReferralSource.SILVER_COIN;
        } else if (referralCount >= 20) {
            return ReferralSource.GAMIFICATION;
        } else {
            return ReferralSource.DIRECT;
        }
    }
}
