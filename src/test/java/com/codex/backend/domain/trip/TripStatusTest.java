package com.codex.backend.domain.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TripStatusTest {

    @Nested
    @DisplayName("fromJson")
    class FromJson {

        @Test
        void returnsNullWhenValueIsNullOrBlank() {
            assertThat(TripStatus.fromJson(null)).isNull();
            assertThat(TripStatus.fromJson(" ")).isNull();
        }

        @Test
        void parsesCanonicalValues() {
            assertThat(TripStatus.fromJson("planned")).isEqualTo(TripStatus.PLANNED);
            assertThat(TripStatus.fromJson("upcoming")).isEqualTo(TripStatus.UPCOMING);
            assertThat(TripStatus.fromJson("active")).isEqualTo(TripStatus.ACTIVE);
            assertThat(TripStatus.fromJson("completed")).isEqualTo(TripStatus.COMPLETED);
        }

        @Test
        void acceptsLegacyAliasesUsedByTheIosClient() {
            assertThat(TripStatus.fromJson("planning")).isEqualTo(TripStatus.PLANNED);
            assertThat(TripStatus.fromJson("ongoing")).isEqualTo(TripStatus.ACTIVE);
            assertThat(TripStatus.fromJson("complete")).isEqualTo(TripStatus.COMPLETED);
        }

        @Test
        void rejectsUnsupportedValues() {
            assertThatThrownBy(() -> TripStatus.fromJson("cancelled"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported trip status");
        }
    }
}
