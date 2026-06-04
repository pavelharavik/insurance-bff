package com.insurance.bff.presentation;

import com.insurance.bff.presentation.exception.HttpException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Formats {@link HttpException} instances into RFC 7807 problem detail responses.
 * Domain-to-HTTP mapping is the responsibility of each controller.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ProblemDetail> handle(HttpException ex) {
        HttpStatusCode status = HttpStatusCode.valueOf(ex.getStatusCode());
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        if (ex.getUpstreamBody() != null) {
            body.setProperty("upstream_body", ex.getUpstreamBody());
        }
        return ResponseEntity.status(status).body(body);
    }
}
