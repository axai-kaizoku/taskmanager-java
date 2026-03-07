package com.example.taskmanager.dao;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAO {
    @Autowired UserRepo userRepo;

    public boolean isUserExistsById(String userId) {
        return userRepo.existsById(userId);
    }

    public User getUserById(String userId) {
        return userRepo.findById(userId).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public void deleteUserById(String userId) {
        userRepo.deleteById(userId);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }
}
