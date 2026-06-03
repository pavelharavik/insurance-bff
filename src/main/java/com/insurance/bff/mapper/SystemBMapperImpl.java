package com.insurance.bff.mapper;

import com.insurance.bff.client.systemb.SystemBResponse;
import com.insurance.bff.model.InsuranceData;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link SystemBMapper}.
 *
 * <ul>
 *   <li>{@code id}         → {@code id}</li>
 *   <li>{@code firstName} + {@code lastName} → {@code name} (null-safe space-joined)</li>
 *   <li>{@code active}     → {@code active}</li>
 *   <li>{@code birthDate}  → not used</li>
 * </ul>
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
