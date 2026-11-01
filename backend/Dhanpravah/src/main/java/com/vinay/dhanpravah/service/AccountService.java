package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.Account;
import com.vinay.dhanpravah.domain.AccountStatus;
import com.vinay.dhanpravah.domain.AppUser;
import com.vinay.dhanpravah.dto.AccountResponse;
import com.vinay.dhanpravah.dto.AccountStatusRequest;
import com.vinay.dhanpravah.dto.AccountValidationResponse;
import com.vinay.dhanpravah.dto.CreateAccountRequest;
import com.vinay.dhanpravah.exception.BusinessRuleException;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final BigDecimal DEFAULT_INITIAL_BALANCE = new BigDecimal("5000.00");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accounts;
    private final UserLookupService users;

    @Transactional
    public AccountResponse createAccount(@Valid CreateAccountRequest request) {
        AppUser owner = users.finsUser(request.userId());
        String currency =
                request.currency() == null || request.currency().isBlank()
                        ? "INR" : request.currency().toUpperCase();

        if (!currency.matches("^[A-Z]{3}$"))
            throw new BusinessRuleException("Currency must be a three-letter code");

        BigDecimal initialBalance = request.initialBalance() == null ? DEFAULT_INITIAL_BALANCE : request.initialBalance();
        Account account = Account.builder()
                .accountNumber(nextAccountNumber())
                .owner(owner)
                .currency(currency)
                .balance(initialBalance)
                .type(request.type())
                .build();
        return AccountResponse.from(accounts.save(account));
    }

    private String nextAccountNumber() {

        for (int attempt = 0; attempt < 20; attempt++) {
            String value = String.format("%021d", Math.floorMod(RANDOM.nextLong(), 1_000_000_000_000L));
            if (!accounts.existsByAccountNumber(value)) return value;
        }
        throw new BusinessRuleException("Could not generate a unique account number");
    }

    public AccountValidationResponse validateRecipient(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("^[0-9]{12}$"))
            return new AccountValidationResponse(false, "Please enter a valid account number.", null);
        return accounts.findByAccountNumber(accountNumber)
                .map(
                        account ->
                                account.getStatus() == AccountStatus.ACTIVE
                                        ? new AccountValidationResponse(
                                        true, "Account found", account.getOwner().getFullName())
                                        : new AccountValidationResponse(false, "Recipient account is inactive.", null))
                .orElseGet(() -> new AccountValidationResponse(false, "User not found.", null));
    }

    public AccountResponse updateAccountStatus(UUID accountId, @Valid AccountStatusRequest request) {
        Account account = findAccount(accountId);
        account.setStatus(request.status());
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        return AccountResponse.from(findAccount(accountId));
    }

    private Account findAccount(UUID accountId) {
        return accounts
                .findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts(Long userId) {
        users.requireUser(userId);
        return accounts.findByOwnerId(userId).stream().map(AccountResponse::from).toList();
    }


}
