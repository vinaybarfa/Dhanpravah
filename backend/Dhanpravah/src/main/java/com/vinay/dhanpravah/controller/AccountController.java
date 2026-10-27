package com.vinay.dhanpravah.controller;

import com.vinay.dhanpravah.dto.AccountResponse;
import com.vinay.dhanpravah.dto.AccountStatusRequest;
import com.vinay.dhanpravah.dto.AccountValidationResponse;
import com.vinay.dhanpravah.dto.CreateAccountRequest;
import com.vinay.dhanpravah.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/users/{userId}/accounts")
    @PreAuthorize("#userId.toString() == authentication.name")
    List<AccountResponse> list(@PathVariable Long userId) {
        return accountService.listAccounts(userId);
    }

    @GetMapping("/accounts/{id}")
    @PreAuthorize("@authorization.canViewAccount(#id, authentication)")
    AccountResponse get(@PathVariable UUID id) {
        return accountService.getAccount(id);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#request.userId.toString() == authentication.name")
    AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/accounts/validate/{accountNumber}")
    AccountValidationResponse validateRecipient(@PathVariable String accountNumber) {
        return accountService.validateRecipient(accountNumber);
    }

    @PatchMapping("/admin/accounts/{id}/status")
    AccountResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody AccountStatusRequest request) {
        return accountService.updateAccountStatus(id, request);
    }

}
