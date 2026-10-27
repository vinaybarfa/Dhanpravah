package com.vinay.dhanpravah.dto;

import com.vinay.dhanpravah.domain.Account;
import com.vinay.dhanpravah.domain.AccountStatus;
import com.vinay.dhanpravah.domain.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType type,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        String holderName
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getOwner().getFullName()
        );
    }
}
