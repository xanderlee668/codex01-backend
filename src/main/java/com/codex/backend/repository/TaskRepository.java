package com.codex.backend.repository;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerOrderByCreatedAtDesc(User owner);
    Optional<Task> findByIdAndOwner(Long id, User owner);
}
