package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.dto.BeneficiaryResponse;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaries;
    private final UserLookupService users;

    public List<BeneficiaryResponse> listBeneficiaries(Long userId) {
        users.requireUser(userId);
        return beneficiaries.findByOwnerId(userId).stream().map(BeneficiaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public void deleteBeneficiary(Long userId, UUID beneficiaryId) {
        var beneficiary =
                beneficiaries
                        .findByIdAndOwnerId(beneficiaryId, userId)
                        .orElseThrow(() -> new NotFoundException("Beneficiary not found"));
        beneficiaries.delete(beneficiary);
    }


}
