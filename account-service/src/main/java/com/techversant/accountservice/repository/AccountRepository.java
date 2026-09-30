/**
 * @file AccountRepository.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Repository interface for performing database operations related to accounts.
 */

package com.techversant.accountservice.repository;

import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.enums.Status;
import com.techversant.accountservice.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Checks if an account exists in the database with the given account number.
     *
     * @param accountNumber the unique account number to check
     * @return true if the account exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Checks if an account exists for the specified user with the given account type.
     *
     * @param customerNo  the unique identifier of the customer
     * @param accountType the type of account to check (e.g., SAVINGS, CURRENT)
     * @return true if an account exists for the user with the given account type, false otherwise
     */
    boolean existsByCustomerNoAndAccountType(Long customerNo, AccountType accountType);

    /**
     * Retrieves an account by its unique identifier and active status.
     *
     * @param id       the unique identifier of the account
     * @param isActive the active status to filter by (true for active accounts, false for inactive accounts)
     * @return the account matching the given id and active status, or null if no match is found
     */
    Account findByIdAndIsActive(UUID id, boolean isActive);

    /**
     * Finds an Account entity by the given customer ID and account status.
     * This method queries the database for an account that matches both:
     * - the specified {@code customerId}, and
     * - the specified {@link Status}.
     *
     * @param customerId the UUID of the customer whose account is being searched
     * @param status     the status of the account (e.g., ACTIVE, INACTIVE)
     * @return the {@link Account} entity matching the customer ID and status, or {@code null} if none found
     */
    Account findByCustomerIdAndStatus(UUID customerId, Status status);

    /**
     * Retrieves a paginated list of accounts filtered by their active status.
     *
     * @param isActive the active status to filter accounts by (true for active accounts, false for inactive accounts)
     * @param pageable the pagination and sorting information
     * @return a paginated list of accounts matching the specified active status
     */
    Page<Account> findAllByIsActive(boolean isActive, Pageable pageable);

    /**
     * Retrieves all Account entities based on their active status.
     * This method queries the database and returns a list of accounts
     * that match the specified {@code isActive} flag.
     *
     * @param isActive true to fetch active accounts, false to fetch inactive accounts
     * @return a {@link List} of {@link Account} entities matching the active status;
     * the list will be empty if no accounts match
     */
    List<Account> findAllByIsActive(boolean isActive);

    /**
     * Calculates the total balance of all accounts in the system.
     * This method uses a JPQL query to sum the {@code balance} field
     * of all {@link Account} entities. If no accounts exist, it returns {@code 0}.
     *
     * @return the total balance of all accounts as a {@link BigDecimal};
     * returns {@code 0} if there are no accounts
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal getTotalBalance();

    /**
     * Counts the total number of accounts with status {@code CLOSED}.
     * This method uses a JPQL query to count all {@link Account} entities
     * where the {@code status} is {@link Status#CLOSED}. If no such accounts exist,
     * it returns {@code 0}.
     *
     * @return the total number of closed accounts as a {@link BigDecimal};
     * returns {@code 0} if no closed accounts are found
     */
    @Query("SELECT COALESCE(COUNT(a), 0) FROM Account a WHERE a.status = CLOSED")
    BigDecimal getTotalClosedAccount();

    /**
     * Counts the total number of accounts with status 'ACTIVE'.
     * This method uses a JPQL query to count all {@link Account} entities
     * where the {@code status} is 'ACTIVE'. If no such accounts exist,
     * it returns {@code 0}.
     *
     * @return the total number of active accounts as a {@link BigDecimal};
     * returns {@code 0} if no active accounts are found
     */
    @Query("SELECT COALESCE(COUNT(a), 0) FROM Account a WHERE a.status = 'ACTIVE'")
    BigDecimal getTotalActiveAccount();

    /**
     * Counts the total number of accounts with status 'INACTIVE'.
     * This method uses a JPQL query to count all {@link Account} entities
     * where the {@code status} is 'INACTIVE'. If no such accounts exist,
     * it returns {@code 0}.
     *
     * @return the total number of inactive accounts as a {@link BigDecimal};
     * returns {@code 0} if no inactive accounts are found
     */
    @Query("SELECT COALESCE(COUNT(a), 0) FROM Account a WHERE a.status = 'INACTIVE'")
    BigDecimal getTotalInActiveAccount();

    /**
     * Retrieves a paginated list of active accounts based on the provided filters.
     * The method applies the following filters if provided:
     * accountNumber – filters by the unique account number
     * accountType – filters by the type of account (enum)
     * currencyId – filters by the associated currency ID
     * If any parameter is {@code null}, that filter is ignored.
     *
     * @param accountNumber the account number to filter by (nullable)
     * @param accountType   the account type (enum) to filter by (nullable)
     * @param currencyId    the ID of the currency to filter by (nullable)
     * @param pageable      the pagination and sorting information
     * @return a paginated list of accounts matching the specified filters and active status
     */
    @Query("SELECT a FROM Account a " +
            "WHERE a.isActive = true " +
            "AND (:accountNumber IS NULL OR a.accountNumber = :accountNumber) " +
            "AND (:accountType IS NULL OR a.accountType = :accountType) " +
            "AND (:currencyId IS NULL OR a.currency.id = :currencyId)" +
            "AND (:customerNo IS NULL OR a.customerNo=:customerNo)" +
            "AND (:status IS NULL OR a.status=:status)")
    Page<Account> filterAccounts(@Param("accountNumber") String accountNumber,
                                 @Param("accountType") AccountType accountType,
                                 @Param("currencyId") Integer currencyId,
                                 @Param("customerNo") Long customerNo,
                                 @Param("status") Status status,
                                 Pageable pageable);


    /**
     * Finds an Account entity by the given customer ID.
     * This method queries the database for an account that matches
     * the specified {@code customerId}.
     *
     * @param customerId the UUID of the customer whose account is being searched
     * @return the {@link Account} entity matching the customer ID, or {@code null} if none found
     */
    Account findByCustomerId(UUID customerId);

    /**
     * Finds an Account entity by the given account number.
     * This method queries the database for an account that matches
     * the specified {@code accountNumber}.
     *
     * @param accountNumber the account number to search for
     * @return the {@link Account} entity matching the account number, or {@code null} if none found
     */
    Account findByAccountNumber(String accountNumber);

    /**
     * Deletes all Account entities that match the given customer number.
     * This method executes a bulk delete operation using a JPQL query.
     * It removes all accounts where the {@code customerNo} matches the provided value.
     *
     * @param customerNo the customer number whose accounts should be deleted
     */
    @Modifying
    @Query("DELETE FROM Account a WHERE a.customerNo = :customerNo")
    void deleteByCustomerNo(@Param("customerNo") Long customerNo);

    /**
     * Finds an Account entity by the given customer number.
     * This method queries the database for an account that matches
     * the specified {@code customerNo}.
     *
     * @param customerNo the customer number to search for
     * @return an {@link Optional} containing the {@link Account} if found,
     * or {@link Optional#empty()} if no matching account exists
     */
    Optional<Account> findByCustomerNo(Long customerNo);
}
