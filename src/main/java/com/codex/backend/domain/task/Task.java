package com.codex.backend.domain.task;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String notes;

    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean completed = false;

    protected Task() {
        // JPA only
    }

    public Task(User owner, String title, String notes, LocalDate dueDate) {
        this.owner = owner;
        this.title = title;
        this.notes = notes;
        this.dueDate = dueDate;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void update(String title, String notes, LocalDate dueDate, boolean completed) {
        this.title = title;
        this.notes = notes;
        this.dueDate = dueDate;
        this.completed = completed;
    }
}
