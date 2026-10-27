package com.vinay.dhanpravah.dto;

public record AccountValidationResponse(
        boolean valid,
        String message,
        String accountHolderName
) {
}
