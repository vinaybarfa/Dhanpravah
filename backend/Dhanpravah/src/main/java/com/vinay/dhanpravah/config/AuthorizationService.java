package com.vinay.dhanpravah.config;

import com.vinay.dhanpravah.repository.AccountRepository;
import com.vinay.dhanpravah.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authorization")
@RequiredArgsConstructor
public class AuthorizationService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public boolean canViewAccount(UUID accountId, Authentication authentication) {
        if (isAdmit(authentication)) return true;
        return accountRepository
                .findById(accountId)
                .map(account -> account.getOwner().getId().toString().equals(authentication.getName()))
                .orElse(false);
    }

    public boolean canViewTransaction(String reference, Authentication authentication) {
        if (isAdmit(authentication)) return true;
        return transactionRepository
                .findByReference(reference)
                .map(transaction -> transaction
                        .getFromAccount()
                        .getOwner()
                        .getId().toString()
                        .equals(authentication.getName())
                    ||
                        transaction.getToAccount()
                                .getOwner()
                                .getId()
                                .toString()
                                .equals(authentication.getName())
                ).orElse(false);
    }

    private boolean isAdmit(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

}
