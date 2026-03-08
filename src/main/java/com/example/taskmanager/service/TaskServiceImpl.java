package com.example.taskmanager.service;

import com.example.taskmanager.dao.*;
import com.example.taskmanager.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.taskmanager.util.AppConstants.FIVE_MINUTES_MS;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final RedisCacheHelper cacheHelper;
    private final TaskDAO taskDAO;
    private final UserDAO userDAO;
    private final ReferAndEarnDAO referAndEarnDAO;
    private final ReferralsDAO referralsDAO;
    private final ReferralClaimsDAO referralClaimsDAO;

    private static final String CACHE_TASK_PREFIX = "task:";

    @Override
    public List<Task> getAllTasks() {
        return taskDAO.getAllTasks();
    }

    @Override
    public Task getTaskById(String id) {
        String key = CACHE_TASK_PREFIX + id;
        Task cachedTask = cacheHelper.get(key, Task.class);
        if (cachedTask != null) {
            return cachedTask;
        }

        Task task = taskDAO.getTaskById(id);
        if (task != null) {
            cacheHelper.set(key, task, FIVE_MINUTES_MS);
        }
        return task;
    }

    @Override
    public Task createTask(Task task, String userId) {
        return createTaskForUser(userId, task);
    }

    @Override
    public Task updateTask(String id, Task taskDetails) {
        String key = CACHE_TASK_PREFIX + id;
        Task task = taskDAO.getTaskById(id);
        if (task == null) {
            throw new RuntimeException("Task not found with id: " + id);
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        
        Task updatedTask = taskDAO.saveTask(task);
        cacheHelper.evict(key);
        return updatedTask;
    }

    @Override
    public void deleteTask(String id) {
        if (!taskDAO.isTaskExistsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskDAO.deleteTaskById(id);
        cacheHelper.evict(CACHE_TASK_PREFIX + id);
    }

    @Override
    public List<Task> getTaskByStatus(TaskStatus status) {
        return taskDAO.getTasksByStatus(status);
    }

    @Override
    public List<Task> searchTasks(String keyword) {
        return taskDAO.searchTasksByTitle(keyword);
    }

    private Task createTaskForUser(String userId, Task task) {
        validateUserExists(userId);
        
        initializeTaskMetadata(task, userId);
        
        processReferralRewards(userId);

        log.info("Creating task for user: {}", userId);
        return taskDAO.saveTask(task);
    }

    @Override
    public List<Task> getTasksByUserId(String userId) {
        validateUserExists(userId);
        return taskDAO.getTaskByUserId(userId);
    }

    private void validateUserExists(String userId) {
        if (!userDAO.isUserExistsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    private void initializeTaskMetadata(Task task, String userId) {
        task.setUserId(userId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
    }

    private void processReferralRewards(String userId) {

        Referrals referrals = referralsDAO.getReferralsByReferralUserId(userId);

        if (referrals == null || referrals.getUserId() == null || referrals.getUserId().isEmpty()) {
            return;
        }

        if (referrals.getSource() == ReferralSource.NO_REFERRER || referrals.getSource() == ReferralSource.DIRECT) {
            return;
        }

        if (referrals.getSource() == ReferralSource.SILVER_COIN && referAndEarnDAO.getReferAndEarnProgressCountByReferralUIdAndSourceId(userId,referrals.getId()) >= 1) {
            return;
        }

        if (referrals.getSource() == ReferralSource.GAMIFICATION && referAndEarnDAO.getReferAndEarnProgressCountByReferralUIdAndSourceId(userId,referrals.getId()) > 20) {
            return;
        }

        log.info("Processing referral rewards for user: {} referred by: {}", userId, referrals.getUserId());
        
        ReferAndEarnProgress progress = createReferAndEarnProgress(userId, referrals);
        
        if (referrals.getSource() == ReferralSource.SILVER_COIN) {
            applySilverCoinReward(progress, referrals);
        }

        referAndEarnDAO.save(progress);
    }

    private ReferAndEarnProgress createReferAndEarnProgress(String userId, Referrals referrals) {
        ReferAndEarnProgress progress = new ReferAndEarnProgress();
        progress.setReferralSourceId(referrals.getId());
        progress.setReferralUId(userId);
        progress.setReferrerUId(referrals.getUserId());
        return progress;
    }

    private void applySilverCoinReward(ReferAndEarnProgress progress, Referrals referrals) {
        progress.setAmount(0);
        progress.setStep(ReferralStep.INVESTMENT_REWARD);
        progress.setState(ReferralState.SUCCESS);

        if (referAndEarnDAO.isReferAndEarnProgressEntryExists(referrals.getReferralUserId())) {
            return;
        }

        // Check if user already collected the reward
        if (referralClaimsDAO.getCountByUserIdAndReferralSourceId(referrals.getUserId(),referrals.getId()) >= 1) {
            return;
        }
        processGoldCouponForReferrer(referrals);
    }

    /**
     * Give coupon - creating ReferralClaims
     * @param referrals
     */
    private void processGoldCouponForReferrer(Referrals referrals) {
        ReferralClaims referralClaims = new ReferralClaims();
        referralClaims.setUserId(referrals.getUserId());
        referralClaims.setClaimRefId(null);
        referralClaims.setReferrerUId(null);
        referralClaims.setRewardType(ReferralRewardType.COUPON);
        referralClaims.setAmount(15);
        referralClaims.setReferralSourceId(referrals.getId());
        referralClaims.setState(ReferralState.SUCCESS);

        log.debug("Silver coin reward: coupon generated for referrer {}", referrals.getUserId());
        referralClaimsDAO.save(referralClaims);
    }
}
