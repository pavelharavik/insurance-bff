package com.insurance.bff.controller;

import com.insurance.bff.model.InsuranceResponse;
import com.insurance.bff.service.InsuranceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for patient insurance lookups.
 */
@RestController
@RequestMapping("/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    /**
     * Returns normalised insurance data for the given patient.
     *
     * @param patientId patient identifier
     * @return insurance response with BFF timestamp
     */
    @GetMapping("/{patientId}")
    public InsuranceResponse getInsurance(@PathVariable String patientId) {
        return InsuranceResponse.from(insuranceService.getInsuranceData(patientId));
    }
}
