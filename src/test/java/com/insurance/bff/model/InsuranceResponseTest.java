package com.insurance.bff.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link InsuranceResponse} serialises to the agreed JSON contract
 * and that its factory method maps fields from {@link InsuranceData} correctly.
 *
 * <p>{@code @JsonTest} loads only the Jackson auto-configuration slice, keeping
 * tests fast while still respecting {@code application.yml} Jackson properties.
 */
@JsonTest
class InsuranceResponseTest {

    private static final String  ID         = "123";
    private static final String  NAME       = "Aliaksei Kozel";
    private static final Instant FIXED_DATE = Instant.parse("2026-05-29T00:00:00Z");
    private static final boolean ACTIVE     = true;

    @Autowired
    private JacksonTester<InsuranceResponse> json;

    // ── Serialisation ─────────────────────────────────────────────────────────

    @Test
    void serialisesAllFieldsWithSnakeCaseNames() throws IOException {
        InsuranceResponse response = new InsuranceResponse(ID, NAME, FIXED_DATE, ACTIVE);

        JsonContent<InsuranceResponse> content = json.write(response);

        assertThat(content).extractingJsonPathStringValue("$.id").isEqualTo(ID);
        assertThat(content).extractingJsonPathStringValue("$.name").isEqualTo(NAME);
        assertThat(content).extractingJsonPathStringValue("$.current_date").isEqualTo("2026-05-29T00:00:00Z");
        assertThat(content).extractingJsonPathBooleanValue("$.is_active").isTrue();
    }

    @Test
    void doesNotExposeJavaBeanFieldNames() throws IOException {
        InsuranceResponse response = new InsuranceResponse(ID, NAME, FIXED_DATE, ACTIVE);

        JsonContent<InsuranceResponse> content = json.write(response);

        assertThat(content).doesNotHaveJsonPath("$.currentDate");
        assertThat(content).doesNotHaveJsonPath("$.active");
    }

    @Test
    void serialisesDateAsIso8601UtcString() throws IOException {
        InsuranceResponse response = new InsuranceResponse(ID, NAME, FIXED_DATE, ACTIVE);

        JsonContent<InsuranceResponse> content = json.write(response);

        assertThat(content).extractingJsonPathStringValue("$.current_date")
                .isEqualTo("2026-05-29T00:00:00Z");
    }

    // ── Factory method ────────────────────────────────────────────────────────

    @Test
    void fromInsuranceData_mapsIdNameActive() {
        InsuranceData data = new InsuranceData(ID, NAME, ACTIVE);

        InsuranceResponse response = InsuranceResponse.from(data);

        assertThat(response.id()).isEqualTo(ID);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.active()).isTrue();
    }

    @Test
    void fromInsuranceData_setsCurrentDateToNow() {
        Instant before = Instant.now();
        InsuranceResponse response = InsuranceResponse.from(new InsuranceData(ID, NAME, ACTIVE));
        Instant after = Instant.now();

        assertThat(response.currentDate()).isBetween(before, after);
    }

    @Test
    void fromInsuranceData_inactivePolicy() {
        InsuranceData data = new InsuranceData(ID, NAME, false);

        assertThat(InsuranceResponse.from(data).active()).isFalse();
    }
}
