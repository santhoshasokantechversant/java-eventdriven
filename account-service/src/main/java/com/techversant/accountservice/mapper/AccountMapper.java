/**
 * @file AccountMapper.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Mapper class for mapping AccountDto class to Account entity class
 */

package com.techversant.accountservice.mapper;

import com.techversant.accountservice.dto.AccountDto;
import com.techversant.accountservice.dto.PaginatedAccountResponseDto;
import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.model.Account;
import com.techversant.accountservice.model.Currency;
import com.techversant.accountservice.repository.CurrencyRepository;
import com.techversant.accountservice.utils.Constants;
import com.techversant.accountservice.utils.exceptions.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AccountMapper {
    private final CurrencyRepository currencyRepository;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final int MAX_PER_MILLI = 999;
    Logger logger = LoggerFactory.getLogger(AccountMapper.class);

    public AccountMapper(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    /**
     * Converts an AccountDto object into an Account entity and populates all required fields.
     * This method:
     * - Generates a new unique 16-digit account number using {@link #generate16DigitNumber()}.
     * - Copies balance, customer number, customer ID, and account number from the DTO.
     * - Converts the account type from String (in DTO) to {@link AccountType} enum.
     * - Fetches the {@link Currency} entity from the database using the provided currency code.
     * - Throws {@link InvalidInputException} if the currency code is invalid or not found.
     *
     * @param accountDto the DTO containing account creation details
     * @param account    the existing Account entity to populate
     * @return the populated Account entity
     * @throws InvalidInputException if the currency code in the DTO is invalid
     */
    public Account accountDtoToEntity(AccountDto accountDto, Account account) {
        account.setAccountNumber(this.generate16DigitNumber());
        account.setBalance(accountDto.getBalance());
        account.setCustomerNo(accountDto.getCustomerNo());
        account.setCustomerId(accountDto.getCustomerId());
        account.setAccNo(accountDto.getAccNo());
        account.setAccountType(AccountType.valueOf(accountDto.getAccountType()));
        Currency currency = currencyRepository.findByCurrencyCode(accountDto.getCurrency());
        if (currency == null) {
            throw new InvalidInputException(Constants.INVALID_CURRENCY_CODE);
        }
        account.setCurrency(currency);
        return account;
    }

    /**
     * Converts a Page of Account entities into a PaginatedAccountResponseDto.
     * This method:
     * - Extracts the list of Account entities from the Page.
     * - Retrieves pagination details such as current page number, total pages, total elements, and page size.
     * - Constructs and returns a PaginatedAccountResponseDto containing both the account list and pagination metadata.
     *
     * @param accountPage the Page object containing Account entities and pagination information
     * @return a PaginatedAccountResponseDto containing the accounts and pagination metadata
     */
    public PaginatedAccountResponseDto accountToAccountResponseDto(Page<Account> accountPage) {
        return new PaginatedAccountResponseDto(
                accountPage.getContent(),
                accountPage.getPageable().getPageNumber(),
                accountPage.getTotalPages(),
                accountPage.getTotalElements(),
                accountPage.getPageable().getPageSize());
    }

    /**
     * Generates a unique 16-digit number using the current time in milliseconds and a counter.
     * The format is: 13-digit-millis + 3-digit-counter, ensuring uniqueness
     * even for multiple calls within the same millisecond.
     * Key features:
     * Thread-safe using synchronized.
     * Retries if the counter exceeds the maximum per millisecond (MAX_PER_MILLI).
     * Handles spurious wakeups using a while loop around wait().
     * Properly handles InterruptedException by preserving the interrupted status
     * and rethrowing as a runtime exception.
     * Rethrows ThreadDeath immediately to avoid interfering with JVM shutdown.
     *
     * @return a unique 16-digit number as a String
     * @throws IllegalStateException if the thread is interrupted while waiting
     */
    public synchronized String generate16DigitNumber() {
        long millis = System.currentTimeMillis();
        int count = COUNTER.getAndIncrement();

        if (count > MAX_PER_MILLI) {
            COUNTER.set(0);
            while (System.currentTimeMillis() == millis) {
                try {
                    wait(1);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread was interrupted while generating number", ex);
                } catch (ThreadDeath td) {
                    throw td;
                }
            }
            return generate16DigitNumber();
        }
        return String.format("%013d%03d", millis, count);
    }
}
