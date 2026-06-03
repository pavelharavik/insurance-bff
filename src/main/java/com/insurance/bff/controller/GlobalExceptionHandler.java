package com.insurance.bff.controller;

import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain exceptions to RFC 7807 problem detail responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @return 404 with the patient ID embedded in the detail message
     */
    @ExceptionHandler(InsuranceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(InsuranceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * @return the status code carried by the exception (500 or 503)
     */
    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ProblemDetail> handleUpstream(UpstreamServiceException ex) {
        HttpStatusCode status = HttpStatusCode.valueOf(ex.getStatusCode());
        return ResponseEntity
                .status(status)
                .body(ProblemDetail.forStatusAndDetail(status, "Upstream service error"));
    }
}
