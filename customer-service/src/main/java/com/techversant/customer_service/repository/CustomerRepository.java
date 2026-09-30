/**
 * @file CustomerRepository.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 27,2025
 * @version 1.0
 * @description Dao for Customer entity
 */

package com.techversant.customer_service.repository;

import com.techversant.customer_service.model.Customer;
import com.techversant.customer_service.utils.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Filters {@link Customer} entities based on the provided optional criteria.
     * Results are paginated according to the provided {@link Pageable} object.
     *
     * @param firstName   optional first name filter (case-insensitive, partial match)
     * @param lastName    optional last name filter (case-insensitive, partial match)
     * @param email       optional email filter (case-insensitive, partial match)
     * @param phoneNumber optional phone number filter (case-insensitive, partial match)
     * @param address     optional address filter (case-insensitive, partial match)
     * @param city        optional city filter (case-insensitive, partial match)
     * @param state       optional state filter (case-insensitive, partial match)
     * @param postalCode  optional postal code filter (case-insensitive, partial match)
     * @param country     optional country filter (case-insensitive, partial match)
     * @param status      required status filter (exact match)
     * @param pageable    pagination and sorting information
     * @return a {@link Page} of {@link Customer} entities matching the filter criteria
     */
    @Query(
            """
                    From Customer c where
                    (:firstName is null or lower(c.firstName) like :firstName)
                    and
                    (:lastName is null or lower(c.lastName) like :lastName)
                    and
                    (:email is null or lower(c.email) like :email)
                    and
                    (:phoneNumber is null or lower(c.phoneNumber) like :phoneNumber)
                    and
                    (:address is null or lower(c.address) like :address)
                    and
                    (:city is null or lower(c.city) like :city)
                    and
                    (:state is null or lower(c.state) like :state)
                    and
                    (:postalCode is null or lower(c.postalCode) like :postalCode)
                    and
                    (:country is null or lower(c.country) like :country)
                    and
                    c.status = :status
                    """
    )
    Page<Customer> filterCustomers(String firstName, String lastName, String email, String phoneNumber, String address, String city, String state, String postalCode, String country, Status status, Pageable pageable);

    /**
     * Retrieves a {@link Customer} by its unique identifier and status.
     *
     * @param id     the unique {@link UUID} of the customer
     * @param status the {@link Status} the customer must have
     * @return the {@link Customer} matching the given ID and status, or {@code null} if not found
     */
    @Query("""
            From Customer c where c.id = :id and c.status = :status
            """)
    Customer getCustomerById(UUID id, Status status);

    /**
     * Retrieves a Customer entity associated with the given user ID.
     *
     * @param userId the UUID of the user
     * @return the Customer entity linked to the provided user ID, or null if not found
     */
    Customer findByUserId(UUID userId);

    /**
     * Retrieves a Customer entity wrapped in an Optional by its customer number.
     *
     * @param customerNo the unique number identifying the customer
     * @return an Optional containing the Customer if found, or empty if not found
     */
    Optional<Customer> findByCustomerNo(Long customerNo);

    /**
     * Retrieves a list of all customers with the specified status.
     *
     * @param status the status to filter customers by
     * @return a list of customers matching the given status
     */
    List<Customer> findAllByStatus(Status status);

    /**
     * Retrieves the next value from the customer number sequence in the database.
     *
     * @return the next customer number as a Long
     */
    @Query(value = "SELECT nextval('customer_schema.customer_no_seq')", nativeQuery = true)
    Long getNextCustomerNumberFromSequence();

    /**
     * Finds the highest customer number currently present in the Customer table.
     *
     * @return the maximum customer number as a Long, or null if no customers exist
     */
    @Query("SELECT MAX(c.customerNo) FROM Customer c")
    Long findMaxCustomerNo();

    /**
     * Finds a customer by their unique ID and status.
     *
     * @param customerId the UUID of the customer
     * @param status     the status of the customer
     * @return the matching Customer entity, or null if not found
     */
    Customer findByCustomerIdAndStatus(UUID customerId, Status status);

    /**
     * Finds a customer by their email address.
     *
     * @param email the email of the customer
     * @return the matching Customer entity, or null if not found
     */
    Customer findByEmail(String email);
}