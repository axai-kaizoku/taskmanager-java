package com.example.taskmanager.service;

import com.example.taskmanager.dao.UserDAO;
import com.example.taskmanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    public List<User> getAllUsers() { return userDAO.getAllUsers(); }

    public User getUserById(String id) { return userDAO.getUserById(id); }

    public void deleteUserById(String id) {
        userDAO.deleteUserById(id);
    }
}
