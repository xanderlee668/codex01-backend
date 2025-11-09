package com.codex.backend.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 发布雪板信息的请求体。
 */
public record CreateListingRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 1000) String description,
        @NotBlank String condition,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @NotBlank @Size(max = 120) String location,
        @NotBlank String tradeOption,
        boolean isFavorite,
        @Size(max = 500) String imageUrl) {
}
