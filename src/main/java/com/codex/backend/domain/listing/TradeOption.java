package com.codex.backend.domain.listing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 交易方式枚举，对应前端的 trade_option 字段。
 */
public enum TradeOption {
    FACE_TO_FACE,
    COURIER,
    HYBRID;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static TradeOption fromJson(String value) {
        if (value == null) {
            return null;
        }
        return TradeOption.valueOf(value.trim().toUpperCase().replace('-', '_').replace(' ', '_'));
    }
}
