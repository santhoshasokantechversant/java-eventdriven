/**
 * @file DashboardController.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description DashboardController class for handling all apis related to the dashboard
 */

package com.techversant.accountservice.controller;

import com.techversant.accountservice.dto.AdminDashboardDto;
import com.techversant.accountservice.dto.ApiResponse;
import com.techversant.accountservice.model.Account;
import com.techversant.accountservice.service.IAccountService;
import com.techversant.accountservice.utils.Constants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final IAccountService iAccountService;

    public DashboardController(IAccountService iAccountService) {
        this.iAccountService = iAccountService;
    }

    /**
     * Retrieves the admin dashboard details.
     * This endpoint:
     * - Calls iAccountService to fetch the AdminDashboardDto containing dashboard metrics and information.
     * - Returns an ApiResponse with success status and dashboard data if available.
     * - Returns an ApiResponse with error status if the dashboard data could not be fetched.
     *
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and dashboard data if available
     * - Status ERROR if fetching dashboard details fails
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> adminDashboard() {
        AdminDashboardDto dashboard = iAccountService.adminDashboard();
        ApiResponse<AdminDashboardDto> apiResponse = new ApiResponse<>();
        if (dashboard == null) {
            apiResponse.setStatus(Constants.STATUS_ERROR);
            apiResponse.setMessage("Failed to Fetch Dashboard Details.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage("Fetch Dashboard Successfully.");
        apiResponse.setData(dashboard);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves the account details for a specific customer by ID.
     * This endpoint:
     * - Accepts a UUID path variable representing the customer ID.
     * - Fetches account details using iAccountService.
     * - Returns an ApiResponse with success status and account data.
     *
     * @param id the UUID of the customer whose account details are being retrieved
     * @return a ResponseEntity containing an ApiResponse with:
     * - Status SUCCESS and the account data for the specified customer
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<ApiResponse<Account>> userDashboard(@Valid @PathVariable UUID id) {
        Account account = iAccountService.getAccountById(id);
        ApiResponse<Account> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(Constants.STATUS_SUCCESS);
        apiResponse.setMessage(Constants.ACCOUNT_FETCH_SUCCESS_MESSAGE);
        apiResponse.setData(account);
        return ResponseEntity.ok(apiResponse);
    }
}
