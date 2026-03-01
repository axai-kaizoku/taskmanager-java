package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.taskmanager.util.AppConstants.FIVE_MINUTES_MS;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisCacheHelper cacheHelper;

    final private String CACHE_TASK_PREFIX = "task:";

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    public Task getTaskById(String id){
        String key = CACHE_TASK_PREFIX+id;
        Task cachedTask = cacheHelper.get(key,Task.class);
        if (cachedTask != null) {
            return cachedTask;
        }
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        cacheHelper.set(key,task,FIVE_MINUTES_MS);
        return task;
    }

    public Task createTask(Task task){
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(String id, Task taskDetails){
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public void deleteTask(String id){
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
        }
        throw new RuntimeException("Task not found with id: " + id);
    }

    public List<Task> getTaskByStatus(TaskStatus status){
        return taskRepository.findByStatus(status);
    }

    public List<Task> searchTasks(String keyword){
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public Task createTaskForUser(String userId, Task task) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        task.setUserId(userId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<Task> getTasksByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return taskRepository.findByUserId(userId);
    }
}