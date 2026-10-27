package com.vinay.dhanpravah.controller;

import com.vinay.dhanpravah.dto.BeneficiaryResponse;
import com.vinay.dhanpravah.dto.CreateBeneficiaryRequest;
import com.vinay.dhanpravah.service.BeneficiaryService;
import com.vinay.dhanpravah.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("#userId.toString() == authentication.name")
    public List<BeneficiaryResponse> listBeneficiaries(@PathVariable Long userId) {
        return beneficiaryService.listBeneficiaries(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#userId.toString() == authentication.name")
    public BeneficiaryResponse createBeneficiary(
            @PathVariable Long userId, @Valid @RequestBody CreateBeneficiaryRequest request) {
        return paymentService.addBeneficiary(userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#userId.toString() == authentication.name")
    public void deleteBeneficiary(@PathVariable Long userId, @PathVariable UUID id) {
        beneficiaryService.deleteBeneficiary(userId, id);
    }


}
