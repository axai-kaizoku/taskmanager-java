package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepo extends MongoRepository<Task, String> {
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByTitleContainingIgnoreCase(String title);
    List<Task> findByUserId(String userId);
}