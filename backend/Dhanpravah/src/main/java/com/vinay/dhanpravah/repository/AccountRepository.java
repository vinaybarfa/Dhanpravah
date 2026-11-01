package com.vinay.dhanpravah.repository;

import com.vinay.dhanpravah.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    @EntityGraph(attributePaths = "owner")
    List<Account> findByOwnerId(Long ownerId);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a join fetch a.owner where a.id=:id")
    Optional<Account> findByIdForUpdate(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a join fetch a.owner where a.accountNumber=:accountNumber")
    Optional<Account> findByAccountNumberForUpdate(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

}