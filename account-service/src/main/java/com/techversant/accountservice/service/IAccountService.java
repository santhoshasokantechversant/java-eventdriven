/**
 * @file IAccountService.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Service interface for handling operations related to account entity
 */

package com.techversant.accountservice.service;

import com.techversant.accountservice.dto.*;
import com.techversant.accountservice.model.Account;
import com.techversant.common_lib.events.AccountsReturnDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAccountService {
    /**
     * Creates a new account using the provided account details.
     *
     * @param accountDto the data transfer object containing account information
     * @return the newly created account entity
     */
    Account createAccount(AccountDto accountDto);

    /**
     * Fetches and compiles data for the admin dashboard.
     * This includes total customers, total active accounts, total balance,
     * and counts of active, inactive, and closed accounts.
     *
     * @return an AdminDashboardDto containing aggregated dashboard metrics
     */
    AdminDashboardDto adminDashboard();

    /**
     * Updates an existing account with the provided details.
     *
     * @param updateAccountDto the data transfer object containing updated account information
     * @param id               the unique identifier of the account to be updated
     * @return the updated account entity
     */
    Account updateAccount(UpdateAccountDto updateAccountDto, UUID id);

    /**
     * Retrieves an active Account by its unique ID.
     *
     * @param id the UUID of the account
     * @return the Account corresponding to the given ID
     */
    Account getAccountByIdDetails(UUID id);

    /**
     * Fetches account information for a given customer ID and returns it as an AccountsReturnDto.
     *
     * @param id the UUID of the customer
     * @return an AccountsReturnDto containing account details for the specified customer
     */
    AccountsReturnDto getAccountBycustomerId(UUID id);

    /**
     * Retrieves the Account entity associated with the specified customer ID.
     *
     * @param id the UUID of the customer
     * @return the Account linked to the given customer ID
     */
    Account getAccountBycustomerIdDetails(UUID id);

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id the unique identifier of the account
     * @return the account entity matching the given id
     */
    Account getAccountById(UUID id);

    /**
     * Deletes an account by its unique identifier.
     *
     * @param id the unique identifier of the account to be deleted
     * @return the deleted account entity
     */
    Account deleteAccountById(UUID id);

    /**
     * Retrieves a paginated list of all accounts.
     *
     * @param pageable the pagination and sorting information
     * @return a paginated response containing account details
     */
    PaginatedAccountResponseDto fetchAllAccounts(Pageable pageable);

    /**
     * Retrieves a paginated list of accounts filtered by account number,account type and currency.
     *
     * @param accountFilterDto the filter criteria containing the account number, account type or currency
     * @return a paginated response containing the filtered account details
     */
    PaginatedAccountResponseDto filterAccounts(AccountFilterDto accountFilterDto);

    /**
     * Retrieves all available currencies from the database in ascending order
     * of their currency code.
     *
     * @return a list of {@link CurrencyDto} objects containing currency details
     */
    List<CurrencyDto> fetchAllCurrency();

    /**
     * Deletes the account associated with the given customer ID.
     *
     * @param id the UUID of the customer whose account should be deleted
     * @throws RuntimeException if an error occurs during deletion
     */
    void deleteFromCustomer(UUID id);

    /**
     * Retrieves an Account entity based on the given account number.
     *
     * @param data the account number of the Account to retrieve
     * @return the Account corresponding to the provided account number,
     * or null if no matching account is found
     */
    Account getAccountByAccountNumber(String data);
}
