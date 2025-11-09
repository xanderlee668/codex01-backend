package com.codex.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 任务创建/更新请求体，字段命名与前端模型 (TaskDTO) 完全一致，方便双方序列化/反序列化。
 */
public record TaskRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1500) String description,
        @Size(max = 100) String category,
        @Size(max = 30) String priority,
        LocalDate dueDate,
        Integer estimatedMinutes,
        List<@Size(max = 30) String> tags,
        boolean completed) {
}
