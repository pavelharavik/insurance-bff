package com.insurance.bff.controller;

import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.exception.UpstreamServiceException;
import com.insurance.bff.model.InsuranceData;
import com.insurance.bff.service.InsuranceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsuranceController.class)
class InsuranceControllerTest {

    private static final String       PATIENT_ID = "123";
    private static final InsuranceData DATA       = new InsuranceData("123", "Alice", true);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InsuranceService insuranceService;

    @Test
    void getInsurance_returns200_withMappedFields() throws Exception {
        when(insuranceService.getInsuranceData(PATIENT_ID)).thenReturn(DATA);

        mockMvc.perform(get("/insurance/{id}", PATIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.is_active").value(true))
                .andExpect(jsonPath("$.current_date").exists());
    }

    @Test
    void getInsurance_returns404_onInsuranceNotFoundException() throws Exception {
        when(insuranceService.getInsuranceData(PATIENT_ID))
                .thenThrow(new InsuranceNotFoundException(PATIENT_ID));

        mockMvc.perform(get("/insurance/{id}", PATIENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No insurance record found for patient: " + PATIENT_ID));
    }

    @Test
    void getInsurance_returns500_onUpstreamServiceException500() throws Exception {
        when(insuranceService.getInsuranceData(PATIENT_ID))
                .thenThrow(new UpstreamServiceException(500));

        mockMvc.perform(get("/insurance/{id}", PATIENT_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Upstream service error"));
    }

    @Test
    void getInsurance_returns503_onUpstreamServiceException503() throws Exception {
        when(insuranceService.getInsuranceData(PATIENT_ID))
                .thenThrow(new UpstreamServiceException(503));

        mockMvc.perform(get("/insurance/{id}", PATIENT_ID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Upstream service error"));
    }
}
