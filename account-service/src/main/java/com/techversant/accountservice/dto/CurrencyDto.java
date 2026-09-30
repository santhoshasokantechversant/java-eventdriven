/**
 * @file CurrencyDto.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 18, 2025
 * @version 1.0
 * @description Dto class for transferring data when fetch a currency.
 */

package com.techversant.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {
    private int id;
    private String currencyCode;
}
