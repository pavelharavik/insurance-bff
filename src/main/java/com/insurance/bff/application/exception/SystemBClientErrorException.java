package com.insurance.bff.application.exception;

import java.util.Map;

/** System B rejected the request with a 4xx response — indicates an integration bug. */
public final class SystemBClientErrorException extends SystemBException {

    public SystemBClientErrorException(Map<String, Object> details) {
        super("System B: client error", details);
    }

    public SystemBClientErrorException() {
        this(Map.of());
    }
}
