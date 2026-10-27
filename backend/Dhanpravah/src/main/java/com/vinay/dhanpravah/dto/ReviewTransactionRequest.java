package com.vinay.dhanpravah.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReviewTransactionRequest(
        @NotNull @Pattern(regexp = "APPROVE|REJECT")
        String decision,
        @Size(max = 255)
        String reason) {

}