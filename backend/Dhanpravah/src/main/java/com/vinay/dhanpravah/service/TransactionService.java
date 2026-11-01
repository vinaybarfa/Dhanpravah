package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.PaymentTransaction;
import com.vinay.dhanpravah.domain.TransactionStatus;
import com.vinay.dhanpravah.dto.PageResponse;
import com.vinay.dhanpravah.dto.TransactionResponse;
import com.vinay.dhanpravah.dto.TransferResponse;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserLookupService userLookupService;


    @Transactional(readOnly = true)
    public TransferResponse getTransaction(String reference) {
        return TransferResponse.from(
                transactionRepository.findByReference(reference)
                        .orElseThrow(() -> new NotFoundException("Transaction not found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> listUserTransactions(Long userId, int page, int size) {
        userLookupService.requireUser(userId);
        return toPageResponse(transactionRepository.findVisibleToUser(userId, pageRequest(page, size)));        
    }

    private PageResponse<TransactionResponse> toPageResponse(Page<PaymentTransaction> result) {
        return new PageResponse<>(
                result.getContent().stream().map(TransactionResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1,size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    public PageResponse<TransactionResponse> listAdminTransactions(TransactionStatus status, int page, int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentTransaction> result =
                status == null
                ? transactionRepository.findAll(pageRequest)
                        : transactionRepository.findByStatus(status, pageRequest);
        return toPageResponse(result);
    }
}
