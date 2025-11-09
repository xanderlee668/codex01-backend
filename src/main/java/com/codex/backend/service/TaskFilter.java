package com.codex.backend.service;

import java.time.LocalDate;
import java.util.Optional;

public record TaskFilter(
        Optional<Boolean> completed,
        Optional<LocalDate> dueFrom,
        Optional<LocalDate> dueTo,
        Optional<String> keyword) {

    public static TaskFilter from(Boolean completed, LocalDate dueFrom, LocalDate dueTo, String keyword) {
        return new TaskFilter(
                Optional.ofNullable(completed),
                Optional.ofNullable(dueFrom),
                Optional.ofNullable(dueTo),
                Optional.ofNullable(keyword).map(String::trim).filter(value -> !value.isEmpty()));
    }
}
