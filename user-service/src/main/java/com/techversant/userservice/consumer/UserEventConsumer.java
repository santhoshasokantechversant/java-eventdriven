package com.techversant.userservice.consumer;

import com.techversant.common_lib.events.*;
import com.techversant.userservice.dto.UserDto;
import com.techversant.userservice.dto.UserUpdateDto;
import com.techversant.userservice.model.ConsumedEvent;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.User;
import com.techversant.userservice.producer.UserEventProducer;
import com.techversant.userservice.repository.ConsumedEventRepository;
import com.techversant.userservice.repository.RoleRepository;
import com.techversant.userservice.repository.UserRepository;
import com.techversant.userservice.service.impl.UserService;
import com.techversant.userservice.utils.enums.UserType;
import com.techversant.userservice.utils.exceptions.EmailAlreadyExistsException;
import com.techversant.userservice.utils.exceptions.RoleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final RoleRepository roleRepository;
    private final ConsumedEventRepository consumedEventRepository;
    private final UserRollbackConsumer userRollbackService;

    @Autowired
    public UserEventConsumer(
            UserService userService,
            UserRepository userRepository,
            UserEventProducer userEventProducer,
            RoleRepository roleRepository,
            ConsumedEventRepository consumedEventRepository,
            UserRollbackConsumer userRollbackService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.roleRepository = roleRepository;
        this.consumedEventRepository = consumedEventRepository;
        this.userRollbackService = userRollbackService;
    }


    @KafkaListener(topics = "customer.created", groupId = "user-service-group")
    public void consumeUserCreated(CustomerCreatedEvent event) {
        if (consumedEventRepository.existsByEventId(event.getEventId())) {
            logger.info("Event already processed: {}", event.getEventId());
            return;
        }
        try {
            logger.info("Creating user for customer: {}", event.getCustomerNo());

            userRollbackService.trackCustomerEmail(event.getCustomerNo(), event.getEmail());

            String username = generateNameBasedUsername(event.getFirstName(), event.getLastName(), event.getCustomerNo());
            if (userRepository.existsByUserName(username)) {
                logger.info("Username exists: {}", username);
                return;
            }

            if (userRepository.existsByEmail(event.getEmail())) {
                throw new EmailAlreadyExistsException("Email exists: " + event.getEmail());
            }

            UserDto userDto = new UserDto();
            userDto.setEmail(event.getEmail());
            userDto.setFirstName(event.getFirstName());
            userDto.setLastName(event.getLastName());
            userDto.setUserName(username);
            userDto.setPhoneNumber(event.getPhoneNumber());
            userDto.setCity(event.getCity());
            userDto.setAddress(event.getAddress());
            userDto.setCountry(event.getCountry());
            userDto.setState(event.getState());
            userDto.setPostalCode(event.getPostalCode());
            userDto.setDateOfBirth(event.getDateOfBirth());
            userDto.setUserType(UserType.CUSTOMER);
            Role customerRole = roleRepository.findByName("Customer")
                    .orElseThrow(() -> new RoleNotFoundException("CUSTOMER role not found"));
            userDto.setRoleId(customerRole.getId().toString());
            User createdUser =  userService.addUser(userDto);
            logger.info("Successfully created user with username: {} for customer: {}",
                    username, event.getCustomerNo());
            ConsumedEvent consumedEvent = new ConsumedEvent(event.getEventId(), event.getEventName());
            consumedEventRepository.save(consumedEvent);

            UserCreatedEvent successEvent = new UserCreatedEvent();
            successEvent.setId(createdUser.getId());
            successEvent.setCreatedAt(java.time.Instant.now().toString());
            successEvent.setStatus("Active");
            successEvent.setUserId(createdUser.getId());
            successEvent.setCustomerId(event.getCustomerId());
            successEvent.setCustomerNo(event.getCustomerNo());
            successEvent.setCorrelationId(event.getCorrelationId());
            successEvent.setUserName(createdUser.getUserName());
            userEventProducer.sendUserCreatedEvent(successEvent);

        } catch (RuntimeException e) {
            logger.error("Failed to create user for customer {}: {}", event.getCustomerNo(), e.getMessage());

            // Clean up tracking on failure
            userRollbackService.removeCustomerMapping(event.getCustomerNo());

            UserCreationFailedEvent failedEvent = new UserCreationFailedEvent();
            failedEvent.setReason("User creation failed: " + e.getMessage());
            failedEvent.setEventId(UUID.randomUUID().toString());
            failedEvent.setTimeStamp(java.time.Instant.now().toString());
            failedEvent.setCustomerNo(event.getCustomerNo());
            failedEvent.setCorrelationId(event.getCorrelationId());
            userEventProducer.sendUserCreationFailedEvent(failedEvent);
        }
    }

    @KafkaListener(topics = "customer.updated", groupId = "user-service-group")
    public void consumeCustomerUpdated(CustomerUpdatedEvent event) {
        try {
            logger.info("Received customer updated event for: {}", event.getCustomerNo());

            // Use email as the primary lookup key (most reliable)
            User existingUser = userRepository.findByEmail(event.getEmail());

            if (existingUser == null) {
                logger.warn("User not found for email: {}, creating new user for customer: {}",
                        event.getEmail(), event.getCustomerNo());
            } else {
                UserUpdateDto userDto = new UserUpdateDto();
                userDto.setEmail(event.getEmail());
                userDto.setFirstName(event.getFirstName());
                userDto.setLastName(event.getLastName());
                userDto.setAddress(event.getAddress());
                userDto.setCity(event.getCity());
                userDto.setCountry(event.getCountry());
                userDto.setDateOfBirth(event.getDateOfBirth());
                userDto.setState(event.getState());
                userDto.setPostalCode(event.getPostalCode());
                userDto.setStatus(event.getStatus());

                userService.updateUser(existingUser.getId(), userDto);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to update user for customer {}: {}", event.getCustomerNo(), e.getMessage());
        }
    }

    private String generateNameBasedUsername(String firstName, String lastName, Long customerNo) {
        return firstName.toLowerCase() + lastName.toLowerCase() + customerNo;
    }

    @KafkaListener(topics = "customer.deleted", groupId = "user-service-group")
    public void consumeCustomerDeleted(CustomerDeletedEvent event) {
        try {
            logger.info("Received customer deleted event for user: {}", event.getUserId());

            if (event.getUserId() != null) {
                userService.deleteUserByCustomer(event.getUserId());
                logger.info("Successfully deleted user: {}", event.getUserId());
            } else {
                logger.warn("No user ID found in customer deleted event for customer: {}", event.getCustomerId());
            }

        } catch (RuntimeException e) {
            logger.error("Failed to delete user for customer {}: {}", event.getCustomerId(), e.getMessage());
            // You can publish a user.deletion.failed event here for compensation
        }
    }

}
