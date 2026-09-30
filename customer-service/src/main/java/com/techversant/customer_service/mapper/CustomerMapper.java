/**
 * @file CustomerMapper.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Mapper class to map Customer entity to different Dto classes
 */

package com.techversant.customer_service.mapper;

import com.techversant.customer_service.dto.CustomerDto;
import com.techversant.customer_service.dto.CustomerResponseDTO;
import com.techversant.customer_service.dto.PaginatedCustomerResponseDto;
import com.techversant.customer_service.model.Customer;
import com.techversant.customer_service.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerMapper {

    /**
     * Converts a Spring Data Page of Customer entities into a PaginatedCustomerResponseDto.
     * This method maps the content, page size, current page, total pages, and total items
     * from the Page object to the response DTO for easier API consumption.
     *
     * @param customers the Page of Customer entities to convert
     * @return a PaginatedCustomerResponseDto containing the paginated customer data
     */
    public PaginatedCustomerResponseDto customerToPaginatedCustomerResponseDto(Page<Customer> customers) {
        PaginatedCustomerResponseDto paginatedCustomerResponseDto = new PaginatedCustomerResponseDto();
        paginatedCustomerResponseDto.setCustomers(customers.getContent());
        paginatedCustomerResponseDto.setPageSize(customers.getSize());
        paginatedCustomerResponseDto.setCurrentPage(customers.getNumber());
        paginatedCustomerResponseDto.setTotalPages(customers.getTotalPages());
        paginatedCustomerResponseDto.setTotalItems(customers.getTotalElements());
        return paginatedCustomerResponseDto;
    }

    /**
     * Converts a CustomerDto to a Customer entity.
     * Maps basic fields like first name, last name, email, and phone number,
     * and sets the default status to ACTIVE.
     *
     * @param dto the CustomerDto to convert
     * @return a Customer entity or null if the input dto is null
     */
    public Customer toEntity(CustomerDto dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setStatus(Status.ACTIVE);
        return customer;
    }

    /**
     * Converts a Customer entity to a CustomerResponseDTO.
     * Maps all relevant fields including customer number, name, contact info, status, and update timestamp.
     *
     * @param customer the Customer entity to convert
     * @return a CustomerResponseDTO or null if the input customer is null
     */
    public CustomerResponseDTO toResponseDTO(Customer customer) {
        if (customer == null) return null;
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setCustomerNo(customer.getCustomerNo());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setStatus(customer.getStatus());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }

    /**
     * Converts a list of Customer entities to a list of CustomerResponseDTOs.
     *
     * @param customers the list of Customer entities
     * @return a list of CustomerResponseDTOs; returns an empty list if input is null
     */
    public List<CustomerResponseDTO> toResponseDTOList(List<Customer> customers) {
        if (customers == null) return new ArrayList<>();
        return customers.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}
