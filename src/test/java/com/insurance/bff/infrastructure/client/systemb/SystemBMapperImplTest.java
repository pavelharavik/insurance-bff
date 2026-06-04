package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.domain.model.InsuranceData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemBMapperImplTest {

    private final SystemBMapper mapper = new SystemBMapperImpl();

    @Test
    void map_mapsAllFields() {
        SystemBResponse response = new SystemBResponse("xyz", "Jane", "Doe", "1990-01-01", true);

        InsuranceData result = mapper.map(response);

        assertThat(result.id()).isEqualTo("xyz");
        assertThat(result.name()).isEqualTo("Jane Doe");
        assertThat(result.active()).isTrue();
    }

    @Test
    void map_joinsFirstAndLastNameWithSpace() {
        SystemBResponse response = new SystemBResponse("1", "Aliaksei", "Kozel", "2000-06-15", false);

        assertThat(mapper.map(response).name()).isEqualTo("Aliaksei Kozel");
    }

    @Test
    void map_activeFalse_whenNotActive() {
        SystemBResponse response = new SystemBResponse("1", "John", "Smith", "1985-03-20", false);

        assertThat(mapper.map(response).active()).isFalse();
    }
}
