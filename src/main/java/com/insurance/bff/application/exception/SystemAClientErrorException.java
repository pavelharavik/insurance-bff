package com.insurance.bff.application.exception;

import java.util.Map;

/** System A rejected the request with a 4xx response — indicates an integration bug. */
public final class SystemAClientErrorException extends SystemAException {

    public SystemAClientErrorException(Map<String, Object> details) {
        super("System A: client error", details);
    }

    public SystemAClientErrorException() {
        this(Map.of());
    }
}
