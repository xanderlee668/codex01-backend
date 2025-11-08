package com.codex.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TaskRepository;
import com.codex.backend.web.dto.TaskRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;
    private User user;

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
        user = new User("user@example.com", "secret", "Test User");
    }

    @Test
    void listTasksReturnsTasksForUser() {
        Task task = new Task(user, "Title", "Notes", LocalDate.now());
        when(taskRepository.findAllByOwnerOrderByCreatedAtDesc(user)).thenReturn(List.of(task));
        List<Task> tasks = taskService.listTasks(user);
        assertThat(tasks).hasSize(1).contains(task);
    }

    @Test
    void getTaskThrowsWhenMissing() {
        when(taskRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTask(user, 1L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createTaskPersistsTask() {
        TaskRequest request = new TaskRequest("Title", "Notes", LocalDate.now(), false);
        Task saved = new Task(user, request.title(), request.notes(), request.dueDate());
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(saved);
        Task result = taskService.createTask(user, request);
        assertThat(result.getTitle()).isEqualTo("Title");
    }
}
