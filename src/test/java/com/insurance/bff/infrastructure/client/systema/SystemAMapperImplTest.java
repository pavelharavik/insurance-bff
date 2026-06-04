package com.insurance.bff.infrastructure.client.systema;

import com.insurance.bff.domain.exception.InsuranceDataUnavailableException;
import com.insurance.bff.domain.model.InsuranceData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemAMapperImplTest {

    private final SystemAMapper mapper = new SystemAMapperImpl();

    @Test
    void map_mapsIdAndParsesNameAndActiveStatus() {
        SystemAResponse response = new SystemAResponse("abc", "Patient: Jane Doe, status: active");

        InsuranceData result = mapper.map(response);

        assertThat(result.id()).isEqualTo("abc");
        assertThat(result.name()).isEqualTo("Jane Doe");
        assertThat(result.active()).isTrue();
    }

    @Test
    void map_activeFalse_whenStatusInactive() {
        SystemAResponse response = new SystemAResponse("1", "Patient: John Smith, status: inactive");

        assertThat(mapper.map(response).active()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"active", "Active", "ACTIVE"})
    void map_activeTrue_caseInsensitive(String status) {
        SystemAResponse response = new SystemAResponse("1", "Patient: John, status: " + status);

        assertThat(mapper.map(response).active()).isTrue();
    }

    @Test
    void map_nameWithComma_parsedCorrectly() {
        SystemAResponse response = new SystemAResponse("1", "Patient: Doe, Jane, status: active");

        assertThat(mapper.map(response).name()).isEqualTo("Doe, Jane");
    }

    @Test
    void map_throwsInsuranceDataUnavailableException_onMalformedDescription() {
        SystemAResponse response = new SystemAResponse("1", "malformed");

        assertThatThrownBy(() -> mapper.map(response))
                .isInstanceOf(InsuranceDataUnavailableException.class);
    }
}
