package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;

import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    Task getTaskById(String id);
    Task createTask(Task task, String userId);
    Task updateTask(String id, Task taskDetails);
    void deleteTask(String id);
    List<Task> getTaskByStatus(TaskStatus status);
    List<Task> searchTasks(String keyword);
    List<Task> getTasksByUserId(String userId);
}
