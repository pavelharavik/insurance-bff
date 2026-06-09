package com.insurance.bff.presentation.insurance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Search criteria supplied by the caller when looking up a patient's insurance record.
 *
 * @param firstName patient's first name
 * @param lastName  patient's last name
 * @param birthDate patient's date of birth in {@code yyyy-MM-dd} format (optional)
 */
public record InsuranceSearchRequest(

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "must be in format yyyy-MM-dd")
    String birthDate

) {

}
