package com.vinay.dhanpravah.controller;

import com.vinay.dhanpravah.domain.TransactionStatus;
import com.vinay.dhanpravah.dto.*;
import com.vinay.dhanpravah.service.PaymentService;
import com.vinay.dhanpravah.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransactionController {
    private final PaymentService paymentService;
    private final TransactionService transactionService;

    @PostMapping("/users/{userId}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#userId.toString() == authentication.name")
    public TransactionResponse transfer(
            @PathVariable Long userId, @Valid @RequestBody CreateTransferRequest request) {
        return paymentService.transfer(userId, request);
    }

    @PostMapping("/users/{userId}/transfers/direct")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#userId.toString() == authentication.name")
    public TransferResponse transferToAccount(
            @PathVariable Long userId, @Valid @RequestBody DirectTransferRequest directTransferRequest
    ) {
        return paymentService.transferToAccount(userId, request);
    }

    @GetMapping("/transactions/{reference}")
    @PreAuthorize("@authorization.canViewTransaction(#reference, authentication)")
    public TransferResponse getTransaction(@PathVariable String reference) {
        return transactionService.getTransaction(reference);
    }

    @GetMapping("/users/{userId}/transactions")
    @PreAuthorize("#userId.toString() == authentication.name")
    public PageResponse<TransactionResponse> listUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return transactionService.listUserTransactions(userId, page, size);
    }

    @GetMapping("/admin/transactions")
    public PageResponse<TransactionResponse> listAdminTransactions(
            @RequestParam(required = false) TransactionStatus transactionStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return transactionService.listAdminTransactions(transactionStatus, page, size);
    }

    @PostMapping("/admin/transactions/{reference}/review")
    public TransferResponse reviewTransaction(
            @PathVariable String reference, @Valid @RequestBody ReviewTransactionRequest request
    ) {
        return paymentService.review(reference, request);
    }
}
