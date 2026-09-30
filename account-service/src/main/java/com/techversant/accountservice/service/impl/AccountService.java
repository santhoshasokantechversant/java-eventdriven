/**
 * @file AccountService.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Class implementing methods of IAccountService interface.
 */

package com.techversant.accountservice.service.impl;

import com.techversant.accountservice.dto.*;
import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.enums.SortDirection;
import com.techversant.accountservice.enums.Status;
import com.techversant.accountservice.mapper.AccountMapper;
import com.techversant.accountservice.model.Account;
import com.techversant.accountservice.model.Currency;
import com.techversant.accountservice.repository.AccountRepository;
import com.techversant.accountservice.repository.CurrencyRepository;
import com.techversant.accountservice.service.IAccountService;
import com.techversant.accountservice.service.webclient.CustomerWebClientService;
import com.techversant.accountservice.utils.Constants;
import com.techversant.accountservice.utils.exceptions.*;
import com.techversant.common_lib.events.AccountsReturnDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CurrencyRepository currencyRepository;
    private final EntityManager entityManager;
    private final CustomerWebClientService customerWebClientService;
    private final HttpServletRequest request;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(HttpServletRequest request, CustomerWebClientService customerWebClientService, EntityManager entityManager, AccountRepository accountRepository, AccountMapper accountMapper, CurrencyRepository currencyRepository) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.currencyRepository = currencyRepository;
        this.entityManager = entityManager;
        this.customerWebClientService = customerWebClientService;
        this.request = request;

    }

    /**
     * Creates a new Account based on the provided AccountDto.
     * This method performs the following steps:
     * 1. Checks if an account with the same account number already exists and
     * throws AccountNumberAlreadyExistsException if true.
     * 2. Checks if an account already exists for the given customer with the specified
     * account type and throws DuplicateAccountException if true.
     * 3. Generates the next customer number and sets it in the AccountDto.
     * 4. Maps the DTO to an Account entity and saves it to the database.
     *
     * @param accountDto the account data transfer object containing details of the account to create
     * @return the created Account entity
     * @throws AccountNumberAlreadyExistsException if an account with the same account number already exists
     * @throws DuplicateAccountException           if an account for the same customer and account type already exists
     */
    @Transactional
    @Override
    public Account createAccount(AccountDto accountDto) {
        boolean accountExists = accountRepository.existsByAccountNumber(accountDto.getAccountNumber());
        if (accountExists) {
            throw new AccountNumberAlreadyExistsException(Constants.ACCOUNT_ALREADY_EXISTS);
        }
        // Check if an account already exists for the given user with the specified account type
        boolean accountExistsUserId = accountRepository.existsByCustomerNoAndAccountType(accountDto.getCustomerNo(), AccountType.valueOf(accountDto.getAccountType()));
        if (accountExistsUserId) {
            throw new DuplicateAccountException(Constants.DUPLICATE_ACCOUNT_EXCEPTION);
        }
        accountDto.setAccNo(generateNextCustomerNumber());
        return accountRepository.save(accountMapper.accountDtoToEntity(accountDto, new Account()));
    }

    /**
     * Updates an existing Account based on the provided UpdateAccountDto and account ID.
     * This method performs the following steps:
     * 1. Retrieves the active account by its ID. Throws AccountNotFoundException if not found.
     * 2. Updates the account balance if provided in the DTO.
     * 3. Updates the account currency if provided and valid; throws InvalidInputException for invalid currency code.
     * 4. Updates the account type if provided in the DTO.
     * 5. Updates the account status if provided in the DTO.
     * 6. Validates that an account cannot be closed if the balance is not zero; throws InvalidInputException if this rule is violated.
     * 7. Saves and returns the updated account entity.
     *
     * @param updateAccountDto the data transfer object containing the fields to update
     * @param id               the UUID of the account to update
     * @return the updated Account entity
     * @throws AccountNotFoundException if no active account is found with the given ID
     * @throws InvalidInputException    if the currency code is invalid or the account cannot be closed due to non-zero balance
     */
    @Override
    public Account updateAccount(UpdateAccountDto updateAccountDto, UUID id) {
        Account existingAccount = accountRepository.findByIdAndIsActive(id, true);
        if (existingAccount == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND);
        }
        if (updateAccountDto.getBalance() != null) {
            existingAccount.setBalance(updateAccountDto.getBalance());
        }
        if (updateAccountDto.getCurrency() != null) {
            Currency currency = currencyRepository.findByCurrencyCode(updateAccountDto.getCurrency());
            if (currency == null) {
                throw new InvalidInputException(Constants.INVALID_CURRENCY_CODE);
            }
            existingAccount.setCurrency(currency);
        }
        if (updateAccountDto.getAccountType() != null) {
            existingAccount.setAccountType(AccountType.valueOf(updateAccountDto.getAccountType()));
        }
        if (updateAccountDto.getStatus() != null) {
            existingAccount.setStatus(Status.valueOf(updateAccountDto.getStatus()));
        }
        if (updateAccountDto.getStatus() != null && updateAccountDto.getStatus().equalsIgnoreCase("closed")
                && updateAccountDto.getBalance() != null && updateAccountDto.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidInputException("Cannot close the account, Balance is not 0");
        }
        return accountRepository.save(existingAccount);
    }

    /**
     * Retrieves dashboard metrics for the admin panel.
     * This method performs the following operations:
     * 1. Validates the presence of a Bearer token in the "Authorization" header.
     * Throws SomethingWentWrongExceptions if no token is provided.
     * 2. Uses the token to fetch the total customer count via CustomerWebClientService.
     * 3. Retrieves all active accounts from the repository.
     * 4. Populates the AdminDashboardDto with metrics including:
     * - total number of accounts
     * - total balance across all accounts
     * - total closed accounts
     * - total active accounts
     * - total inactive accounts
     *
     * @return an AdminDashboardDto containing the aggregated dashboard metrics
     * @throws SomethingWentWrongExceptions if the Authorization header is missing or invalid
     */
    @Override
    public AdminDashboardDto adminDashboard() {
        AdminDashboardDto adminDashboardDto = new AdminDashboardDto();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SomethingWentWrongExceptions("No JWT token received from gateway");
        }
        String token = authHeader.substring(7);

        // Use DTO directly for validation call
        Integer customerCount = customerWebClientService.getUserCount(token).block();
        adminDashboardDto.setCustomerCount(customerCount);
        List<Account> accounts = accountRepository.findAllByIsActive(true);
        adminDashboardDto.setTotalAccounts(accounts.size());
        adminDashboardDto.setTotalAmount(accountRepository.getTotalBalance());
        adminDashboardDto.setTotalClosedAccounts(accountRepository.getTotalClosedAccount());
        adminDashboardDto.setActiveAccounts(accountRepository.getTotalActiveAccount());
        adminDashboardDto.setInactiveAccounts(accountRepository.getTotalInActiveAccount());
        return adminDashboardDto;
    }

    /**
     * Retrieves the Account associated with the given UUID.
     * This method performs the following steps:
     * 1. Validates the presence of a Bearer token in the "Authorization" header.
     * Throws SomethingWentWrongExceptions if no token is provided.
     * 2. Extracts the token and fetches customer data via CustomerWebClientService.
     * 3. Retrieves the account associated with the fetched customer ID from the repository.
     * 4. Throws AccountNotFoundException if no account is found for the customer.
     *
     * @param id the UUID of the account to retrieve
     * @return the Account entity associated with the given ID
     * @throws SomethingWentWrongExceptions if the Authorization header is missing or invalid
     * @throws AccountNotFoundException     if no account is found for the given customer ID
     */
    @Override
    public Account getAccountById(UUID id) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SomethingWentWrongExceptions("No JWT token received from gateway");
        }
        String token = authHeader.substring(7);
        ApiResponse<CustomerDto> user = customerWebClientService.getUserData(token, id).block();
        Account account = user != null ? accountRepository.findByCustomerId(user.getData().getCustomerId()) : null;
        if (account == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    /**
     * Retrieves an active Account by its UUID.
     * This method fetches the account from the repository where the account ID matches the provided UUID
     * and the account is marked as active.
     *
     * @param id the UUID of the account to retrieve
     * @return the active Account entity corresponding to the given ID
     * @throws AccountNotFoundException if no active account is found with the specified ID
     */
    @Override
    public Account getAccountByIdDetails(UUID id) {
        Account account = accountRepository.findByIdAndIsActive(id, true);
        if (account == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    /**
     * Retrieves account details for a given customer ID.
     * This method performs the following:
     * 1. Fetches the Account entity associated with the provided customer UUID.
     * 2. Throws AccountNotFoundException if no account is found for the given customer ID.
     * 3. Maps the Account entity to an AccountsReturnDto including:
     * - account ID
     * - customer number
     * - account number
     * - account type
     * - balance
     * - customer ID
     * - active status
     * - account status
     *
     * @param id the UUID of the customer whose account details are to be retrieved
     * @return an AccountsReturnDto containing the account details
     * @throws AccountNotFoundException if no account exists for the specified customer ID
     */
    @Override
    public AccountsReturnDto getAccountBycustomerId(UUID id) {
        Account account = accountRepository.findByCustomerId(id);
        if (account == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND); // or throw custom exception if needed
        }
        return AccountsReturnDto.builder()
                .id(account.getId())
                .customerNo(account.getCustomerNo())
                .accNo(account.getAccNo())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().toString())
                .balance(account.getBalance())// safe null check
                .customerId(account.getCustomerId())
                .isActive(account.isActive())
                .status(account.getStatus().toString())
                .build();
    }

    /**
     * Retrieves the Account entity associated with the given customer ID.
     * This method fetches the account from the repository based on the provided customer UUID.
     * If no account exists for the customer, an AccountNotFoundException is thrown.
     *
     * @param id the UUID of the customer whose account is to be retrieved
     * @return the Account entity corresponding to the given customer ID
     * @throws AccountNotFoundException if no account is found for the specified customer ID
     */
    @Override
    public Account getAccountBycustomerIdDetails(UUID id) {
        Account account = accountRepository.findByCustomerId(id);
        if (account == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    /**
     * Performs a soft delete of an active Account by its ID.
     * This method:
     * 1. Retrieves the account with the specified UUID that is currently active.
     * 2. Throws AccountNotFoundException if no active account exists for the given ID.
     * 3. Sets the account's active status to false to mark it as deleted.
     * 4. Saves the updated account entity back to the repository.
     *
     * @param id the UUID of the account to delete
     * @return the Account entity after being marked as inactive
     * @throws AccountNotFoundException if no active account is found with the specified ID
     */
    @Override
    public Account deleteAccountById(UUID id) {
        Account existingAccount = accountRepository.findByIdAndIsActive(id, true);
        if (existingAccount == null) {
            throw new AccountNotFoundException(Constants.ACCOUNT_NOT_FOUND);
        }
        existingAccount.setActive(false);
        return accountRepository.save(existingAccount);
    }

    /**
     * Retrieves a paginated list of all active accounts.
     * This method fetches all accounts marked as active from the repository
     * and maps them to a PaginatedAccountResponseDto for easier pagination handling.
     *
     * @param pageable the Pageable object containing page number, size, and sort information
     * @return a PaginatedAccountResponseDto containing the list of active accounts and pagination metadata
     */
    @Override
    public PaginatedAccountResponseDto fetchAllAccounts(Pageable pageable) {
        return accountMapper.accountToAccountResponseDto(accountRepository.findAllByIsActive(true, pageable));
    }

    /**
     * Filters accounts based on the provided criteria and returns a paginated response.
     * This method supports dynamic filtering on:
     * - Account number
     * - Account type
     * - Currency ID
     * - Customer number
     * - Account status
     * The results are paginated and sorted according to the parameters in AccountFilterDto.
     *
     * @param accountFilterDto the DTO containing filtering, sorting, and pagination information
     * @return a PaginatedAccountResponseDto containing the filtered list of accounts and pagination metadata
     */
    @Override
    public PaginatedAccountResponseDto filterAccounts(AccountFilterDto accountFilterDto) {
        Sort sort = accountFilterDto.getSortDirection() == SortDirection.ASC
                ? Sort.by(accountFilterDto.getSortField()).ascending()
                : Sort.by(accountFilterDto.getSortField()).descending();
        Pageable pageable = PageRequest.of(accountFilterDto.getPage(), accountFilterDto.getSize(), sort);

        // Call single repository method with dynamic filters
        Page<Account> accounts = accountRepository.filterAccounts(
                accountFilterDto.getAccountNumber(),
                accountFilterDto.getAccountType(),
                accountFilterDto.getCurrencyId(),
                accountFilterDto.getCustomerNo(),
                accountFilterDto.getStatus(),
                pageable
        );

        // Map to PaginatedAccountResponseDto
        return new PaginatedAccountResponseDto(
                accounts.getContent(),
                accounts.getNumber(),
                accounts.getTotalPages(),
                accounts.getTotalElements(),
                accounts.getSize()
        );
    }

    /**
     * Retrieves all currencies from the repository in ascending order of their currency codes.
     * Each Currency entity is mapped to a CurrencyDto containing its ID and currency code.
     *
     * @return a list of CurrencyDto representing all available currencies sorted by currency code
     */
    @Override
    public List<CurrencyDto> fetchAllCurrency() {
        return currencyRepository.findAllByOrderByCurrencyCodeAsc()
                .stream()
                .map(c -> new CurrencyDto(c.getId(), c.getCurrencyCode()))
                .toList();
    }

    /**
     * Generates the next unique customer number using a database sequence.
     * This method executes a native SQL query on the sequence `account_schema.account_no_seq`
     * and returns its next value as a Long.
     *
     * @return the next value of the customer number sequence
     */
    private Long generateNextCustomerNumber() {
        Query query = entityManager.createNativeQuery("SELECT nextval('account_schema.account_no_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Deletes the account associated with the given customer ID.
     * This method retrieves the account for the specified customer and removes it
     * from the repository.
     *
     * @param id the UUID of the customer whose account should be deleted
     */
    @Override
    public void deleteFromCustomer(UUID id) {
        Account account = this.accountRepository.findByCustomerId(id);
        accountRepository.delete(account);
    }

    /**
     * Retrieves an account by its account number.
     *
     * @param data the account number to search for
     * @return the Account entity matching the provided account number, or null if not found
     */
    @Override
    public Account getAccountByAccountNumber(String data) {
        return accountRepository.findByAccountNumber(data);
    }

    /**
     * Rolls back the account creation process by deleting the account associated with the given customer number.
     * This method attempts to remove the account from the repository and logs the operation.
     * If any exception occurs during deletion, it is logged and wrapped in a RuntimeException.
     *
     * @param customerNo the customer number of the account to be rolled back
     * @throws RuntimeException if the rollback operation fails
     */
    public void rollbackAccountCreation(Long customerNo) {
        try {
            // Delete account by customer number
            accountRepository.deleteByCustomerNo(customerNo);

            logger.info("Rolled back account creation for customer: {}", customerNo);

        } catch (RuntimeException e) {
            throw new AccountRollbackFailedException("Account rollback failed"+ e);
        }
    }

    /**
     * Rolls back the account associated with the given customer number.
     * This method attempts to find the account by customer number and delete it from the repository.
     * Logs are generated to indicate the start, success, or failure of the rollback operation.
     * If the account is not found, a warning is logged. Any exception during deletion is logged
     * and rethrown as a RuntimeException.
     *
     * @param customerNo the customer number whose account should be rolled back
     * @throws RuntimeException if the rollback operation fails due to an exception
     */
    public void rollbackAccountByCustomerNo(Long customerNo) {
        try {
            logger.info("🔄 ACCOUNT ROLLBACK STARTED FOR CUSTOMER: {}", customerNo);

            // Find account by customer number
            Optional<Account> accountOptional = accountRepository.findByCustomerNo(customerNo);

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                // Delete the account
                accountRepository.delete(account);
                accountRepository.flush();

                logger.info("✅ ACCOUNT ROLLBACK COMPLETED FOR CUSTOMER: {}", customerNo);
            } else {
                logger.warn("⚠️ ACCOUNT NOT FOUND FOR CUSTOMER: {}", customerNo);
            }

        } catch (RuntimeException e) {
            throw new AccountRollbackFailedException("Account rollback failed"+ e);
        }
    }

}
