package com.codex.backend.domain.task;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.StringListConverter;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1500)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 30)
    private String priority;

    private LocalDate dueDate;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags", length = 500)
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private boolean completed = false;

    protected Task() {
        // JPA only
    }

    public Task(User owner, String title, String description, LocalDate dueDate) {
        this(owner, title, description, null, null, dueDate, null, List.of(), false);
    }

    public Task(
            User owner,
            String title,
            String description,
            String category,
            String priority,
            LocalDate dueDate,
            Integer estimatedMinutes,
            List<String> tags,
            boolean completed) {
        this.owner = owner;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
        this.completed = completed;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void update(
            String title,
            String description,
            String category,
            String priority,
            LocalDate dueDate,
            Integer estimatedMinutes,
            List<String> tags,
            boolean completed) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.completed = completed;
    }
}
