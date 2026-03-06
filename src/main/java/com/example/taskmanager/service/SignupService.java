package com.example.taskmanager.service;

import com.example.taskmanager.ReferralsDAO;
import com.example.taskmanager.dto.SignupRequest;
import com.example.taskmanager.model.ReferralSource;
import com.example.taskmanager.model.Referrals;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ReferralsRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class SignupService {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public ReferralsRepository referralsRepository;

    @Autowired
    public ReferralsDAO referralsDAO;

    final private String SIGNUP_SERVICE = "SIGNUP SERVICE: ";

    public void signUpUser(SignupRequest signUpRequest, String referrerUserId) {
        if (userRepository.findByEmail(signUpRequest.getEmail()) != null) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> roles = getRoles(signUpRequest);

        user.setRoles(roles);
        userRepository.save(user);
        log.info(SIGNUP_SERVICE + "REFERRER USER ID FOUND");
        Referrals referralsEntry = new Referrals();
        if (referrerUserId == null || referrerUserId.isEmpty()) {
            referralsEntry.setUserId(null);
            referralsEntry.setSource(ReferralSource.NO_REFERRER);
        } else {
            referralsEntry.setUserId(referrerUserId);
            ReferralSource referralSource = referralsDAO.getReferralSourceByUserId(referrerUserId);
            System.out.println(referralSource);
            System.out.println(referralsDAO.getReferralsCountByUserId(referrerUserId));
            if (referralSource == null) {
                referralsEntry.setSource(ReferralSource.DIRECT);
            } else {
                referralsEntry.setSource(referralSource);
            }
        }
        referralsEntry.setReferralUserId(user.getId());
        referralsRepository.save(referralsEntry);
    }

    private static Set<String> getRoles(SignupRequest signUpRequest) {
        Set<String> strRoles = signUpRequest.getRoles();
        Set<String> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add("ROLE_USER");
        } else {
            strRoles.forEach(role -> {
                switch (role) {
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
}
