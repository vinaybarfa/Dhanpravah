package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.*;
import com.vinay.dhanpravah.dto.*;
import com.vinay.dhanpravah.exception.BusinessRuleException;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("50000.00");
    private static final DateTimeFormatter REFERENCE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").withZone(ZoneOffset.UTC);

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Transactional
    public BeneficiaryResponse addBeneficiary(Long ownerId, CreateBeneficiaryRequest request) {
        AppUser owner =
                userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User not found"));

        Account destination = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new BusinessRuleException("Beneficiary account was not found"));

        if (destination.getOwner().getId().equals(ownerId)) {
            throw new BusinessRuleException("A beneficiary cannot be one of your own accounts");
        }
        Beneficiary beneficiary = Beneficiary.builder()
                .accountNumber(request.accountNumber())
                .owner(owner)
                .nickname(request.nickname())
                .name(request.name())
                .build();
        return BeneficiaryResponse.from(beneficiaryRepository.save(beneficiary));
    }

    public TransactionResponse transfer(Long ownerId, CreateTransferRequest request) {
        Beneficiary beneficiary =
                beneficiaryRepository
                        .findByIdAndOwnerId(request.beneficiaryId(), ownerId)
                        .orElseThrow(() -> new NotFoundException("Beneficiary not found"));

        Account sender = accountRepository.findByIdForUpdate(request.fromAccountId())
                .orElseThrow(() -> new NotFoundException("Source account not found"));

        Account receiver =
                accountRepository.findByAccountNumber(beneficiary.getAccountNumber())
                        .orElseThrow(() -> new BusinessRuleException("Beneficiary account was not found"));

        validateTransfer(ownerId, sender, receiver, request.amount());

        PaymentTransaction paymentTransaction = PaymentTransaction
                .builder()
                .amount(request.amount())
                .description(request.description())
                .reference(nextReference())
                .fromAccount(sender)
                .toAccount(receiver)
                .status(TransactionStatus.SUCCESS)
                .build();

        if (request.amount().compareTo(REVIEW_THRESHOLD) >= 0) {
            paymentTransaction.flag("Transaction amount exceeded the configured review limit.", "HIGH");
        } else {
            sender.debit(request.amount());
            receiver.credit(request.amount());
        }

        TransactionResponse response = TransactionResponse.from(transactionRepository.save(paymentTransaction));
        notifyTransfer(sender, receiver, paymentTransaction);
        return response;
    }

    private void validateTransfer(Long ownerId, Account sender, Account receiver, BigDecimal amount) {
        if (!sender.getOwner().getId().equals(ownerId)) {
            throw new BusinessRuleException("Source account does not belong to the current user");
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessRuleException("You cannot transfer money to the same account.");
        }

        if (sender.getStatus() != AccountStatus.ACTIVE
                || receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Sender and recipient accounts must be active");
        }
        if (!sender.getCurrency().equals(receiver.getCurrency())) {
            throw new BusinessRuleException("Cross-currency transfers are not supported");
        }
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient balance.");
        }
    }

    private void notifyTransfer(Account sender, Account receiver, PaymentTransaction transaction) {
        String statusText = transaction.getStatus() == TransactionStatus.FLAGGED
                ? "is under review"
                : "was completed successfully";
        String message = "Transfer "
                + transaction.getReference()
                + " for "
                + transaction.getAmount()
                + " "
                + transaction.getCurrency()
                + " "
                + statusText
                + ".";

        notificationService.transferAlert(sender.getOwner(), "Transfer update", message);
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            notificationService.transferAlert(
                    receiver.getOwner(),
                    "Money received",
                    "You received" +
                            transaction.getAmount()
                            + " " + transaction.getCurrency()
                            + " from a Dhanpravah account."
            );
        }
    }

    private String nextReference() {
        return "PF-TXN-"
                + REFERENCE_TIME_FORMAT.format(Instant.now())
                + String.format("%03d", new Random().nextInt(1000));
    }

    public TransferResponse transferToAccount(Long ownerId, DirectTransferRequest request) {
        Account sender =
                accountRepository
                        .findByIdForUpdate(request.senderAccountId())
                        .orElseThrow(() -> new NotFoundException("Source account not found"));

        if (!request.recipientAccountNumber().matches("^[0-9]{12}$")) {
            throw new BusinessRuleException("Please enter a valid account number.");
        }
        Account receiver =
                accountRepository
                        .findByAccountNumberForUpdate(request.recipientAccountNumber())
                        .orElseThrow(() -> new BusinessRuleException("User not found."));

        validateTransfer(ownerId, sender, receiver, request.amount());
        sender.debit(request.amount());
        receiver.credit(request.amount());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .reference(nextReference())
                .fromAccount(sender)
                .toAccount(receiver)
                .amount(request.amount())
                .description(request.description())
                .status(TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);
        notifyTransfer(sender, receiver, transaction);
        return TransferResponse.builder()
                .success(true)
                .message("Money transferred successfully.")
                .amount(request.amount())
                .transactionId(transaction.getReference())
                .build();
    }

    @Transactional
    public TransferResponse review(String reference, ReviewTransactionRequest request) {
        PaymentTransaction transaction =
                transactionRepository
                        .findByReference(reference)
                        .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.FLAGGED) {
            throw new BusinessRuleException("Only flagged transactions can be reviewed");
        }

        if ("APPROVE".equals(request.decision())) {
            approveFlaggedTransaction(transaction);
        } else {
            String reason = request.reason() == null || request.reason().isBlank()
                    ? "Rejected during review"
                    : request.reason();
            transaction.reject(reason);
        }
        return TransferResponse.from(transaction);
    }


    private void approveFlaggedTransaction(PaymentTransaction transaction) {
        Account sender = accountRepository.findByIdForUpdate(transaction.getFromAccount().getId()).orElseThrow();
        Account receiver = accountRepository.findByIdForUpdate(transaction.getToAccount().getId()).orElseThrow();

        if (sender.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient account balance to approve this transfer");
        }

        sender.debit(transaction.getAmount());
        receiver.credit(transaction.getAmount());
        transaction.approve();


    }
}
