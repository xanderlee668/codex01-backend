package com.codex.backend.domain.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 雪板成色枚举，对应前端 API 中的 condition 字段。
 */
public enum ListingCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    WORN;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ListingCondition fromJson(String value) {
        if (value == null) {
            return null;
        }
        return ListingCondition.valueOf(value.trim().toUpperCase().replace('-', '_').replace(' ', '_'));
    }
}
