package com.vinay.dhanpravah.repository;

import com.vinay.dhanpravah.domain.PaymentTransaction;
import com.vinay.dhanpravah.domain.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    @EntityGraph(
            attributePaths = {"fromAccount", "fromAccount.owner", "toAccount", "toAccount.owner"}
    )
    Optional<PaymentTransaction> findByReference(String reference);

    @Query(
            "select t from PaymentTransaction t where t.fromAccount.owner.id=:userId or"
            + " t.toAccount.owner.id=:userId"
    )
    Page<PaymentTransaction> findVisibleToUser(Long userId, Object o);

    @EntityGraph(
            attributePaths = {"fromAccount", "fromAccount.owner", "toAccount", "toAccount.owner"}
    )
    Page<PaymentTransaction> findByStatus(TransactionStatus status, PageRequest pageRequest);

    @Override
    @EntityGraph(
            attributePaths = {"fromAccount", "fromAccount.owner", "toAccount", "toAccount.owner"})
    Page<PaymentTransaction> findAll(Pageable pageable);
}