package com.university.academic.taskmanagement.service;

import com.university.academic.taskmanagement.domain.Task;
import com.university.academic.taskmanagement.domain.TaskRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@PreAuthorize("isAuthenticated()")
public class TaskService {

    private final TaskRepository taskRepository;

    private final Clock clock;

    TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTask(String description, @Nullable LocalDate dueDate) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        var task = new Task();
        task.setDescription(description);
        task.setCreationDate(clock.instant());
        task.setDueDate(dueDate);
        taskRepository.saveAndFlush(task);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

}
