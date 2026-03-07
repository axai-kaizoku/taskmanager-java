package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    @Autowired
    public UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully",users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new RuntimeException("User not found with id "+id);
        }
        return ResponseEntity.ok(ApiResponse.success("User found",user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        if (user != null) {
            userService.deleteUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", user));
        } else {
            return ResponseEntity.ok(ApiResponse.error("User not found with id " + id));
        }
    }
}
