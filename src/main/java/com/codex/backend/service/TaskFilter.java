package com.codex.backend.service;

import java.time.LocalDate;
import java.util.Optional;

public record TaskFilter(
        Optional<Boolean> completed,
        Optional<LocalDate> dueFrom,
        Optional<LocalDate> dueTo,
        Optional<String> keyword,
        Optional<String> category,
        Optional<String> priority,
        Optional<String> tag) {

    public static TaskFilter from(
            Boolean completed,
            LocalDate dueFrom,
            LocalDate dueTo,
            String keyword,
            String category,
            String priority,
            String tag) {
        return new TaskFilter(
                Optional.ofNullable(completed),
                Optional.ofNullable(dueFrom),
                Optional.ofNullable(dueTo),
                normalize(keyword),
                normalize(category),
                normalize(priority),
                normalize(tag));
    }

    private static Optional<String> normalize(String raw) {
        return Optional.ofNullable(raw).map(String::trim).filter(value -> !value.isEmpty());
    }
}
