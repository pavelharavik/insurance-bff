package com.insurance.bff.infrastructure.client.systemb;

import com.insurance.bff.domain.model.InsuranceData;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link SystemBMapper}.
 */
@Component
public class SystemBMapperImpl implements SystemBMapper {

    @Override
    public InsuranceData map(SystemBResponse response) {
        String name = Stream.of(response.firstName(), response.lastName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        return new InsuranceData(response.id(), name, response.active());
    }
}
