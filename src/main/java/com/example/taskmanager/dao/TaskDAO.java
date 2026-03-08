package com.example.taskmanager.dao;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskDAO {
    @Autowired TaskRepo taskRepo;

    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    public Task getTaskById(String taskId) {
        return taskRepo.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public List<Task> getTaskByUserId(String userId) {
        return taskRepo.findByUserId(userId);
    }

    public Task saveTask(Task task) {
        return taskRepo.save(task);
    }

    public boolean isTaskExistsById(String taskId) {
        return taskRepo.existsById(taskId);
    }

    public void deleteTaskById(String taskId) {
        taskRepo.deleteById(taskId);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepo.findByStatus(status);
    }

    public List<Task> searchTasksByTitle(String keyword) {
        return  taskRepo.findByTitleContainingIgnoreCase(keyword);
    }


}

