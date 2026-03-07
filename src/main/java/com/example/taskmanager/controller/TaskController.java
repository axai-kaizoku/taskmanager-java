package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.security.UserDetailsImpl;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@PreAuthorize("isAuthenticated()")
public class TaskController {
    @Autowired
    public TaskService taskService;

    // Get all tasks
    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(){
        log.info("Fetching all tasks");
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", tasks));
    }

    // Get all tasks by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable String id){
        log.info("Fetching task with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Task found", taskService.getTaskById(id)));
    }

    // Create new task
    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody Task task){
        log.info("Creating a new task: {}", task.getTitle());
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        String userId = ((UserDetailsImpl) principal).getId();
        Task createdTask = taskService.createTask(task,userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", createdTask));
    }

    // Update task
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable String id, @Valid @RequestBody Task task) {
        log.info("Updating task with ID: {}", id);
        try {
            Task updatedTask = taskService.updateTask(id, task);
            return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updatedTask));
        } catch(RuntimeException e){
            log.error("Error updating task with ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Task not found with id: " + id));
        }
    }

    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable String id){
        log.warn("Deleting task with ID: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("Fetching tasks with status: {}", status);
        List<Task> tasks = taskService.getTaskByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully for status: " + status, tasks));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Task>>> searchTasks(@RequestParam String keyword){
        log.info("Searching tasks with keyword: {}", keyword);
        List<Task> tasks = taskService.searchTasks(keyword);
        return ResponseEntity.ok(ApiResponse.success("Tasks searched successfully with keyword: " + keyword, tasks));
    }

    // Create task for specific user
    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Task>> createTaskForUser(@PathVariable String userId, @Valid @RequestBody Task task) {
        log.info("Creating task for user: {}. Task: {}", userId, task.getTitle());
        Task createdTask = taskService.createTask(task, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully for user: " + userId, createdTask));
    }

    // Get all tasks for specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByUserId(@PathVariable String userId) {
        log.info("Fetching tasks for user ID: {}", userId);
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully for user: " + userId, tasks));
    }
}
