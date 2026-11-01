package com.vinay.dhanpravah.dto;

import com.vinay.dhanpravah.domain.PaymentTransaction;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransferResponse(
        boolean success,
        String message,
        String transactionId,
        BigDecimal amount
) {

    public static TransferResponse from(PaymentTransaction transaction) {
    }
}
