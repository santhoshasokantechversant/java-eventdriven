/**
 * @file CurrencyRepository.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Repository interface for performing database operations related to currencies.
 */

package com.techversant.accountservice.repository;

import com.techversant.accountservice.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
    /**
     * Retrieves a currency entity by its currency code.
     *
     * @param currencyCode the ISO or custom currency code to search for (e.g., "USD", "EUR")
     * @return the currency entity matching the given currency code, or null if no match is found
     */
    Currency findByCurrencyCode(String currencyCode);

    /**
     * Retrieves all currency records from the database, ordered in ascending order
     * by their currency code.
     *
     * @return a list of {@link Currency} entities sorted by currency code
     */
    List<Currency> findAllByOrderByCurrencyCodeAsc();
}
