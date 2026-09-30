/**
 * @file AccountController.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description AccountController class for handling all apis related to the account
 */

package com.techversant.accountservice.controller;

import com.techversant.accountservice.dto.*;
import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.enums.SortDirection;
import com.techversant.accountservice.enums.Status;
import com.techversant.accountservice.model.Account;
import com.techversant.accountservice.service.IAccountService;
import com.techversant.accountservice.service.UserIdentityService;
import com.techversant.accountservice.utils.Constants;
import com.techversant.common_lib.events.AccountsReturnDto;
import com.techversant.common_lib.utility.AuditLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/account")
public class AccountController {

    private final IAccountService iAccountService;
    private final UserIdentityService userIdentityService;

    public AccountController(IAccountService iAccountService, UserIdentityService userIdentityService) {
        this.iAccountService = iAccountService;
        this.userIdentityService = userIdentityService;
    }

    /**
     * Handles the creation of a new account.
     * This endpoint:
     * - Accepts an AccountDto in the request body.
     * - Creates a new Account using the iAccountService.
     * - Returns an ApiResponse with success or error status.
     * - Logs all actions for auditing purposes, including success, failure, or exceptions.
     *
     * @param accountDto the account details to create
     * @param request    the HttpServletRequest used to retrieve the current user and client IP
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and account data if creation succeeds,
     * - Status ERROR if creation fails or an exception occurs,
     * with HTTP 500 status in case of unexpected errors
     */
    @PostMapping("/create-account")
    public ResponseEntity<ApiResponse<Account>> createAccount(
            @Valid @RequestBody AccountDto accountDto,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            Account account = iAccountService.createAccount(accountDto);
            ApiResponse<Account> apiResponse = new ApiResponse<>();

            if (account.getId() == null) {
                apiResponse.setStatus(Constants.STATUS_ERROR);
                apiResponse.setMessage(Constants.ACCOUNT_CREATED_FAILED);

                // AUDIT LOG: Account creation failed
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        Constants.ACCOUNT,                                // entity
                        "CREATE_ACCOUNT",                         // action
                        Constants.FAILED                                  // status
                );

                return ResponseEntity.ok(apiResponse);
            }

            apiResponse.setStatus(Constants.STATUS_SUCCESS);
            apiResponse.setMessage(Constants.ACCOUNT_CREATED);
            apiResponse.setData(account);

            // AUDIT LOG: Account created successfully
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    "CREATE_ACCOUNT: " + account.getId(),     // action with account ID
                    Constants.SUCCESS                                // status
            );

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            // AUDIT LOG: Account creation exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    "CREATE_ACCOUNT - Error: " + e.getMessage(), // action
                    "ERROR"                                   // status
            );

            ApiResponse<Account> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(Constants.STATUS_ERROR);
            apiResponse.setMessage("Account creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Handles updating an existing account by ID.
     * This endpoint:
     * - Accepts an UpdateAccountDto in the request body and a UUID path variable.
     * - Updates the account using the iAccountService.
     * - Returns an ApiResponse with success or error status.
     * - Logs all actions for auditing purposes, including success, failure, or exceptions.
     *
     * @param updateAccountDto the updated account details
     * @param id               the UUID of the account to update
     * @param request          the HttpServletRequest used to retrieve the current user and client IP
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and updated account data if the update succeeds,
     * - Status ERROR if the update fails or an exception occurs,
     * with HTTP 500 status in case of unexpected errors
     */
    @PutMapping("/update-account/{id}")
    public ResponseEntity<ApiResponse<Account>> updateAccount(
            @Valid @RequestBody UpdateAccountDto updateAccountDto,
            @PathVariable UUID id,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            Account account = iAccountService.updateAccount(updateAccountDto, id);
            ApiResponse<Account> apiResponse = new ApiResponse<>();

            if (account.getId() == null) {
                apiResponse.setStatus(Constants.STATUS_ERROR);
                apiResponse.setMessage(Constants.ACCOUNT_UPDATED_FAILED);

                // AUDIT LOG: Account update failed
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        Constants.ACCOUNT,                                // entity
                        Constants.UPDATE_ACCOUNT + id,                  // action with account ID
                        Constants.FAILED                                  // status
                );

                return ResponseEntity.ok(apiResponse);
            }

            apiResponse.setStatus(Constants.STATUS_SUCCESS);
            apiResponse.setMessage(Constants.ACCOUNT_UPDATED);
            apiResponse.setData(account);

            // AUDIT LOG: Account updated successfully
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    Constants.UPDATE_ACCOUNT + id,                  // action with account ID
                    Constants.SUCCESS                                 // status
            );

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            // AUDIT LOG: Account update exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    Constants.UPDATE_ACCOUNT + id + " - Error: " + e.getMessage(), // action with error
                    "ERROR"                                   // status
            );

            ApiResponse<Account> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(Constants.STATUS_ERROR);
            apiResponse.setMessage("Account update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Retrieves the details of an account by its ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the account ID.
     * - Fetches the account details using iAccountService.
     * - Returns an ApiResponse with success status and account data if found.
     *
     * @param id the UUID of the account to retrieve
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and account data if the account exists
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccountById(@Valid @PathVariable UUID id) {
        Account account = iAccountService.getAccountByIdDetails(id);
        ApiResponse<Account> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }


    /**
     * Retrieves account details associated with a specific customer ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the customer ID.
     * - Fetches the account details using iAccountService.
     * - Returns an ApiResponse with success status and account data if found.
     *
     * @param id the UUID of the customer whose account is being retrieved
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and account data associated with the customer
     */
    @GetMapping("/get-account-by-customer-id/{id}")
    public ResponseEntity<ApiResponse<AccountsReturnDto>> getAccountBycustomerId(@Valid @PathVariable UUID id) {
        AccountsReturnDto account = iAccountService.getAccountBycustomerId(id);
        ApiResponse<AccountsReturnDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves detailed account information associated with a specific customer ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the customer ID.
     * - Fetches detailed account information using iAccountService.
     * - Returns an ApiResponse with success status and account data if found.
     *
     * @param id the UUID of the customer whose detailed account information is being retrieved
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and detailed account data associated with the customer
     */
    @GetMapping("/get-account-by-customer-id-details/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccountBycustomerIdDetails(@Valid @PathVariable UUID id) {
        Account account = iAccountService.getAccountBycustomerIdDetails(id);
        ApiResponse<Account> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deletes an account by its ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the account ID to delete.
     * - Deletes the account using iAccountService.
     * - Returns an ApiResponse with success status and deleted account data if deletion succeeds.
     * - Returns an ApiResponse with error status if deletion fails or an exception occurs.
     * - Logs all actions for auditing purposes, including success or failure with error details.
     *
     * @param id      the UUID of the account to delete
     * @param request the HttpServletRequest used to retrieve the current user and client IP
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and deleted account data if deletion succeeds
     * - Status ERROR with HTTP 500 in case of exceptions
     */
    @DeleteMapping("/delete-account/{id}")
    public ResponseEntity<ApiResponse<Account>> deleteAccountById(
            @Valid @PathVariable UUID id,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            Account account = iAccountService.deleteAccountById(id);
            ApiResponse<Account> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(Constants.STATUS_SUCCESS);
            apiResponse.setMessage("Account deleted successfully");
            apiResponse.setData(account);

            // AUDIT LOG: Account deleted successfully
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    "DELETE_ACCOUNT: " + id,                  // action with account ID
                    Constants.SUCCESS                                  // status
            );

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            // AUDIT LOG: Account deletion exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    Constants.ACCOUNT,                                // entity
                    "DELETE_ACCOUNT: " + id + " - Error: " + e.getMessage(), // action with error
                    Constants.FAILED                                  // status
            );

            ApiResponse<Account> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(Constants.STATUS_ERROR);
            apiResponse.setMessage("Account deletion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Fetches all accounts with pagination and sorting.
     * This endpoint:
     * - Accepts optional request parameters for page number, page size, sort field, and sort direction.
     * - Retrieves paginated account data using iAccountService.
     * - Returns an ApiResponse with success status and paginated account data.
     *
     * @param page          the page number to retrieve (default is 0)
     * @param size          the number of accounts per page (default is 10)
     * @param sortField     the field to sort by (default is "createdAt")
     * @param sortDirection the direction to sort (ASC or DESC, default is DESC)
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and paginated account data
     */
    @GetMapping("/fetch-all-accounts")
    public ResponseEntity<ApiResponse<PaginatedAccountResponseDto>> fetchAllAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "DESC") SortDirection sortDirection) {
        Sort sort = sortDirection == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PaginatedAccountResponseDto account = iAccountService.fetchAllAccounts(PageRequest.of(page, size, sort));
        ApiResponse<PaginatedAccountResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Filters accounts based on the criteria provided in AccountFilterDto.
     * This endpoint:
     * - Accepts an AccountFilterDto in the request body containing filter criteria.
     * - Retrieves filtered and paginated account data using iAccountService.
     * - Returns an ApiResponse with success status and filtered account data.
     *
     * @param accountFilterDto the filter criteria for fetching accounts
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and filtered account data
     */
    @PostMapping("/filter-accounts")
    public ResponseEntity<ApiResponse<PaginatedAccountResponseDto>> filterAccounts(@RequestBody AccountFilterDto accountFilterDto) {
        PaginatedAccountResponseDto account = iAccountService.filterAccounts(accountFilterDto);
        ApiResponse<PaginatedAccountResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves a list of all available currencies.
     * This endpoint:
     * - Fetches all currency data using iAccountService.
     * - Returns an ApiResponse with success status and the list of currencies.
     *
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and a list of CurrencyDto objects
     */
    @GetMapping("/fetch-all-currency")
    public ResponseEntity<ApiResponse<List<CurrencyDto>>> fetchAllCurrency() {
        List<CurrencyDto> currency = iAccountService.fetchAllCurrency();
        ApiResponse<List<CurrencyDto>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage("Currency fetched successfully");
        apiResponse.setData(currency);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves all possible account types.
     * This endpoint:
     * - Returns all values of the AccountType enum.
     * - Wraps the result in an ApiResponse with success status.
     *
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and an array of AccountType values
     */
    @GetMapping("/account-types")
    public ResponseEntity<ApiResponse<AccountType[]>> getAccountTypes() {
        AccountType[] result = AccountType.values();
        ApiResponse<AccountType[]> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage("Account type fetched successfully");
        apiResponse.setData(result);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deletes customer-related data by customer ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the customer ID.
     * - Calls iAccountService to delete associated customer data.
     * - Returns a 204 No Content response upon successful deletion.
     *
     * @param id the UUID of the customer whose data should be deleted
     * @return a ResponseEntity with HTTP 204 No Content status
     */
    @DeleteMapping("/customer-delete/{id}")
    public ResponseEntity<Void> deleteFromCustomer(@PathVariable UUID id) {
        iAccountService.deleteFromCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all possible account statuses.
     * This endpoint:
     * - Returns all values of the Status enum.
     * - Wraps the result in an ApiResponse with success status.
     *
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and an array of Status values
     */
    @GetMapping("/account-status")
    public ResponseEntity<ApiResponse<Status[]>> getAccountStatus() {
        Status[] result = Status.values();
        ApiResponse<Status[]> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage("Account status fetched successfully");
        apiResponse.setData(result);
        return ResponseEntity.ok(apiResponse);
    }
}
