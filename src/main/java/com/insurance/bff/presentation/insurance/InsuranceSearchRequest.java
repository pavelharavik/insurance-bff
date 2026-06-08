package com.insurance.bff.presentation.insurance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Search criteria supplied by the caller when looking up a patient's insurance record.
 *
 * @param firstName patient's first name
 * @param lastName  patient's last name
 * @param birthDate patient's date of birth; must be a past date
 */
public record InsuranceSearchRequest(

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @Past
    LocalDate birthDate

) {

}
