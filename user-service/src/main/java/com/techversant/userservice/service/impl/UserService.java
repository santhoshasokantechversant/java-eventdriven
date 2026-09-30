/**
 * @file UserService.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Class implementing methods of IUserService interface
 */

package com.techversant.userservice.service.impl;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.CustomerReturnDto;
import com.techversant.common_lib.events.UserDetailsDto;
import com.techversant.common_lib.utility.AuditLogger;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.mapper.UserMapper;
import com.techversant.userservice.model.*;
import com.techversant.userservice.repository.*;
import com.techversant.userservice.service.IPrivilegeService;
import com.techversant.userservice.service.IRoleService;
import com.techversant.userservice.service.IUserService;
import com.techversant.userservice.service.keyclock.KeyclockUserService;
import com.techversant.userservice.service.webclient.CustomerServiceWebClient;
import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.ResetPasswordStatus;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.enums.UserType;
import com.techversant.userservice.utils.exceptions.*;
import com.techversant.userservice.utils.services.AESUtil;
import com.techversant.userservice.utils.services.EmailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import static com.techversant.userservice.utils.Constants.*;

@Service
public class UserService implements IUserService {

    private final KeyclockUserService keyclockUserService;
    private final IRoleService iRoleService;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final IPrivilegeService iPrivilegeService;
    private final EntityManager entityManager;
    private final CustomerServiceWebClient customerServiceWebClient;
    private final HttpServletRequest request;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AESUtil aesUtil;
    private final ResetPasswordRepository resetPasswordRepository;
    private final EndpointRepository endpointRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final IUserService selfProxy;

    @Value("${service.forget-password-url}")
    private String forgotPasswordFrontendUrl;
    @Value("${service.register-url}")
    private String registerFrontendUrl;
    @Value("${service.admin-initial-password:}")
    private String adminInitialPassword;

    public UserService(EndpointRepository endpointRepository, ResetPasswordRepository resetPasswordRepository, AESUtil aesUtil, TemplateEngine templateEngine, EmailService emailService, HttpServletRequest request, CustomerServiceWebClient customerServiceWebClient, EntityManager entityManager, UserRepository userRepository, IPrivilegeService iPrivilegeService, RoleRepository roleRepository, UserRoleRepository userRoleRepository, UserMapper userMapper, KeyclockUserService keyclockUserService, IRoleService iRoleService,@Lazy IUserService selfProxy) {
        this.keyclockUserService = keyclockUserService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.iRoleService = iRoleService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.iPrivilegeService = iPrivilegeService;
        this.entityManager = entityManager;
        this.customerServiceWebClient = customerServiceWebClient;
        this.request = request;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.aesUtil = aesUtil;
        this.resetPasswordRepository = resetPasswordRepository;
        this.endpointRepository = endpointRepository;
        this.selfProxy = selfProxy;
    }

    /**
     * @param userDto the data transfer object containing user information to be added
     * @return the saved User entity
     * @throws UserNameAlreadyExistsException if the username is already taken by an active user
     * @throws EmailAlreadyExistsException    if the email is already registered to an active user
     */

    @Override
    public User addUser(UserDto userDto) {
        checkUserExistence(userDto);

        UUID roleId = UUID.fromString(userDto.getRoleId());
        Role role = this.iRoleService.roleById(roleId);
        if (role.getId() == null) {
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }
        userDto.setUserType(UserType.USER);
        if(role.getName().equals("Customer")){
            userDto.setUserType(UserType.CUSTOMER);
        }
        setUserDefaults(userDto, role);

        String keyclock = this.keyclockUserService.createUser(userDto);
        if (isValidKeyclockId(keyclock)) {
            userDto.setKeyclockUserId(keyclock);
            User user = this.userRepository.save(userMapper.userDtoToEntity(userDto, new User()));
            if (user.getId() == null) {
                throw new SomethingWentWrongException(USER_CREATED_FAILED);
            }
            if (user.getUserType().equals(UserType.CUSTOMER)){
                // via the proxy so @Async applies (a this.sendEmail call would run synchronously)
                selfProxy.sendEmail(user, role.getName());
            }

            this.userRoleRepository.save(userMapper.userRoleDtoToEntity(user, role, new UserRole()));
            return user;
        } else {
            throw new SomethingWentWrongException(FAILED_TO_SAVE_USERS_KEYCLOCK);
        }
    }

    private void checkUserExistence(UserDto userDto) {
        if (this.userRepository.findOneByUserNameAndIsActive(userDto.getUserName(), true) != null) {
            throw new UserNameAlreadyExistsException(USER_ALREADY_EXISTS);
        }
        if (this.userRepository.findOneByEmailAndIsActive(userDto.getEmail(), true) != null) {
            throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
        }
        if (this.userRepository.findOneByPhoneNumberAndIsActive(userDto.getPhoneNumber(), true) != null) {
            throw new PhoneNumberAlreadyExistException("Phone number already exists.");
        }
    }

    private void setUserDefaults(UserDto userDto, Role role) {
        userDto.setUserNo(generateNextCustomerNumber());
        userDto.setRoleName(role.getName());

        if (userDto.getUserName() == null || userDto.getUserName().isEmpty()) {
            userDto.setUserName(setUserName(userDto.getFirstName(), userDto.getLastName()));
        }

        if (userDto.getUserType() == null || userDto.getUserType() == UserType.CUSTOMER) {
            userDto.setUserType(UserType.CUSTOMER);
        } else {
            userDto.setUserType(UserType.USER);
        }
    }

    private boolean isValidKeyclockId(String keyclock) {
        return keyclock != null && !keyclock.isBlank();
    }

    @Async
    public void sendEmail(User user, String role){
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", user.getFirstName() + " " + user.getLastName());
        placeholders.put("userName", user.getUserName());
        placeholders.put("email", user.getEmail());
        placeholders.put("phoneNumber", user.getPhoneNumber());
        placeholders.put("role", role);
        placeholders.put("link", registerFrontendUrl);
        try {
            Context context = new Context();
            context.setVariables(placeholders);

            // This processes the template and replaces th:text and th:href variables
            String htmlContent = templateEngine.process("UserCreation.html", context);
            emailService.sendHtml(user.getEmail(), "User Created Successfully", htmlContent);
        } catch (RuntimeException e) {
            throw new EmailSendFailedException("Failed to send user creation email", e);
        }

    }

    @Override
    public User createAdmin() {
        if (adminInitialPassword == null || adminInitialPassword.isBlank()) {
            throw new SomethingWentWrongException("ADMIN_INITIAL_PASSWORD is not configured.");
        }
        try{
            User duplicate = this.userRepository.findByStaticUserAndIsActive(PermissionType.YES, true);
            if (duplicate != null) {
                throw new SomethingWentWrongException("Admin already Created Once");
            }
            RoleDto roleDto = new RoleDto();
            roleDto.setName("Admin");
            roleDto.setDescription("Admin is more Powerful.");
            roleDto.setPosition("1");
            roleDto.setRoleCreate(PermissionType.YES);
            Role role;
            role = this.roleRepository.findOneByNameAndIsActive(roleDto.getName(), true);
            if (role == null) {
                role = this.iRoleService.addRole(roleDto);
                if (role == null) {
                    throw new SomethingWentWrongException(ROLE_CREATED_FAILED);
                }
            }
            UserDto userDto = new UserDto();
            userDto.setEmail("admin@gmail.com");
            userDto.setRoleId(role.getId().toString());
            userDto.setRoleName(role.getName());
            userDto.setFirstName("Admin");
            userDto.setLastName("A");
            userDto.setPhoneNumber("88888888");
            userDto.setPasswordHash(adminInitialPassword);
            userDto.setUserName(setUserName(userDto.getFirstName(), userDto.getLastName()));
            userDto.setStaticUser(PermissionType.YES);
            userDto.setUserNo(generateNextCustomerNumber());
            userDto.setUserType(UserType.USER);
            userDto.setRegistered(PermissionType.YES);
            User user = this.addUser(userDto);
            if (user == null) {
                throw new SomethingWentWrongException(USER_CREATED_FAILED);
            }
            List<String> httpMethods = new ArrayList<>(Arrays.asList("post", "put", "get", "delete"));
            List<Endpoint> endpoints = new ArrayList<>();
            for(String val: httpMethods) {
                Endpoint endpoint = new Endpoint();
                endpoint.setHttpMethod(val);
                endpoints.add(endpoint);
            }
            this.endpointRepository.saveAll(endpoints);
            return user;
        } catch (SomethingWentWrongException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.error("Admin creation failed", e);
            throw new SomethingWentWrongException("Failed to create Admin: " + e.getMessage(), e);
        }
    }

    public static String setUserName(String firstName, String lastName) {
        firstName = firstName.toLowerCase();
        lastName = formatLastName(lastName);
        return firstName + lastName + generateFourDigitNumber();
    }

    public static String formatLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return "";
        }

        // convert to lowercase and trim spaces
        lastName = lastName.trim().toLowerCase();

        // if length is less than 3, return as is
        if (lastName.length() <= 3) {
            return lastName;
        }

        // else return only first 3 characters
        return lastName.substring(0, 3);
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static int generateFourDigitNumber() {
        // Random number between 1000 and 9999
        return 1000 + SECURE_RANDOM.nextInt(9000);
    }

    /**
     * Retrieves a paginated list of all active users.
     *
     * @param pageable pagination and sorting information for querying users
     * @return a PaginatedUserResponseDto containing the paginated list of active users and pagination metadata
     */
    @Override
    public PaginatedUserResponseDto getAllUsers(Pageable pageable) {
        Page<User> users = this.userRepository.findAllByIsActive(true, pageable);
        return userMapper.userToPaginatedUserResponseDto(users);
    }

    /**
     * Retrieves an active user by their unique ID and maps the entity to a display DTO.
     *
     * @param id the UUID of the user to retrieve
     * @return a UserDisplayDto if the user is found and active; otherwise, null
     */
    @Override
    public UserDisplayDto getUserById(UUID id) {
        User user = this.userRepository.findByIdAndIsActive(id, true);
        if (user == null) {
            return null;
        }

        if (user.getRoleId() == null) {
            UserRole userRole = userRoleRepository.findOneByUser(user);
            if (userRole != null) { // optional safety check
                user.setRoleId(userRole.getRole().getId());
            }
        }

        return userMapper.userEntityToUserDisplayDto(user);
    }


    /**
     * Filters active users based on optional criteria such as first name, last name, username, and email.
     *
     * @param userFilterDto the DTO containing filter conditions, pagination, and sorting settings
     * @return a PaginatedUserResponseDto containing the filtered and paginated list of users
     */
    @Override
    public PaginatedUserResponseDto filterUsers(UserFilterDto userFilterDto) {
        if (userFilterDto.getFirstName() != null && userFilterDto.getFirstName().isBlank()) {
            userFilterDto.setFirstName(null);
        } else if (userFilterDto.getFirstName() != null) {
            userFilterDto.setFirstName("%" + userFilterDto.getFirstName().toLowerCase() + "%");
        }
        if (userFilterDto.getLastName() != null && userFilterDto.getLastName().isBlank()) {
            userFilterDto.setLastName(null);
        } else if (userFilterDto.getLastName() != null) {
            userFilterDto.setLastName("%" + userFilterDto.getLastName().toLowerCase() + "%");
        }
        if (userFilterDto.getUserName() != null && userFilterDto.getUserName().isBlank()) {
            userFilterDto.setUserName(null);
        } else if (userFilterDto.getUserName() != null) {
            userFilterDto.setUserName("%" + userFilterDto.getUserName().toLowerCase() + "%");
        }
        if (userFilterDto.getEmail() != null && userFilterDto.getEmail().isBlank()) {
            userFilterDto.setEmail(null);
        } else if (userFilterDto.getEmail() != null) {
            userFilterDto.setEmail("%" + userFilterDto.getEmail().toLowerCase() + "%");
        }

        Sort sort = userFilterDto.getSortDirection() == SortDirection.ASC ? Sort.by(userFilterDto.getSortField()).ascending() : Sort.by(userFilterDto.getSortField()).descending();
        Pageable pageable = PageRequest.of(userFilterDto.getPage(), userFilterDto.getSize(), sort);
        Page<User> users = this.userRepository.filterUsers(userFilterDto.getFirstName(), userFilterDto.getLastName(), userFilterDto.getUserName(), userFilterDto.getEmail(),userFilterDto.getRoleId(), true, pageable);
        return userMapper.userToPaginatedUserResponseDto(users);
    }

    /**
     * Updates an existing user's details based on the provided user ID and update data.
     *
     * @param id            the UUID of the user to be updated
     * @param userUpdateDto a DTO containing optional fields for updating the user's first and/or last name
     * @return a UserDisplayDto representing the updated user
     * @throws UserNotFoundException     if no user exists with the provided ID
     * @throws IllegalArgumentException  if a provided name field is blank
     * @throws UpdateUserFailedException if an error occurs during the update/save operation
     */
    @Override
    public User updateUser(UUID id, UserUpdateDto userUpdateDto) {
        try {
            User user = userRepository.findByIdAndIsActive(id, true);
            if (user == null) {
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            userRepository.deleteByRoleId(user.getId());
            Role role = roleRepository.findByIdAndIsActive(user.getRoleId(), true);
            if (role == null) {
                UserRole userRole = userRoleRepository.findOneByUser(user);
                role = userRole.getRole();
            }
            user.setFirstName(userUpdateDto.getFirstName());
            user.setLastName(userUpdateDto.getLastName());
            user.setCountry(userUpdateDto.getCountry());
            user.setState(userUpdateDto.getState());
            user.setCity(userUpdateDto.getCity());
            user.setPostalCode(userUpdateDto.getPostalCode());
            user.setAddress(userUpdateDto.getAddress());
            user.setDateOfBirth(userUpdateDto.getDateOfBirth());
            user.setRoleId(UUID.fromString(userUpdateDto.getRoleId()));

            this.keyclockUserService.updateUser(user.getKeyclockUserId(), user, role.getName());
            if (userUpdateDto.getStatus() != null && !userUpdateDto.getStatus().isEmpty()) {
                if (userUpdateDto.getStatus().equalsIgnoreCase("inactive")) {
                    user.setActive(false);
                } else if (userUpdateDto.getStatus().equalsIgnoreCase("active")) {
                    user.setActive(true);
                }
            }
            this.userRoleRepository.save(userMapper.userRoleDtoToEntity(user, role, new UserRole()));
            return this.userRepository.save(user);
        } catch (RuntimeException e) {
            throw new UpdateUserFailedException(USER_UPDATE_FAILED);
        }
    }


    @Override
    public User updateUserPassword(UUID id, UpdatePasswordDto userUpdateDto) {
        try {
            User user = userRepository.findByIdAndIsActive(id, true);
            if (user == null) {
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            LoginDto loginDto = new LoginDto();
            loginDto.setEmail(user.getEmail());
            loginDto.setPasswordHash(userUpdateDto.getOldPassword());
            TokenDto keyClock = this.keyclockUserService.login(loginDto);
            if (keyClock.getValid().equalsIgnoreCase(STATUS_ERROR)) {
                throw new UpdateUserFailedException(keyClock.getMessage());
            }
            this.keyclockUserService.updateUserPassword(user.getKeyclockUserId(), userUpdateDto);
            return user;
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException(e.getMessage());
        }
    }


    @Override
    public User updateUserRole(UUID id, UpdateUserRole updateUserRole) {
        try {
            User user = userRepository.findByIdAndIsActive(id, true);
            if (user == null) {
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            UUID roleId = UUID.fromString(updateUserRole.getRoleId());
            Role role = roleRepository.findOneByIdAndIsActive(roleId, true);
            if (role == null) {
                throw new RoleNotFoundException(ROLE_NOT_FOUND);
            }
            this.keyclockUserService.assignRoleToUser(user.getKeyclockUserId(), role.getKeyclockRoleId());
            UserRole userRole = this.userRoleRepository.findOneByUser(user);
            userRole.setRole(role);
            this.userRoleRepository.save(userRole);
            return user;
        } catch (RuntimeException e) {
            throw new UpdateUserFailedException(USER_UPDATE_FAILED);
        }
    }

    /**
     * Performs a soft delete of a user by setting their active status to false.
     *
     * @param id the UUID of the user to be deleted
     * @return a ResponseEntity containing an ApiResponse with success status
     * and a confirmation message
     * @throws UserNotFoundException     if no user exists with the provided ID
     * @throws DeleteUserFailedException if an error occurs while performing the delete operation
     */

    public void deleteUserByCustomer(UUID id) {
        try {
            User user = userRepository.findByIdAndIsActive(id, true);
            if (user == null) {
                throw new UserNotFoundException(USER_NOT_FOUND);
            }
            this.keyclockUserService.deleteUser(user.getKeyclockUserId());
            userRepository.deleteByRoleId(user.getId());
            userRepository.delete(user);
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to delete User.", e);
        }
    }


    public ResponseEntity<ApiResponse<JsonObject>> forgotPasswordMailSend(ForgetPasswordDto loginDto) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        String ip = request.getRemoteAddr();
        User user = null;

        try {
            user = this.userRepository.findByUserName(loginDto.getEmail());
            if (user == null || user.getId() == null) {
                user = this.userRepository.findByEmail(loginDto.getEmail());
                if (user == null || user.getId() == null) {
                    AuditLogger.log(loginDto.getEmail(), ip, FORGOT_PASSWORD, INVALIDEMORUN, STATUS_FAILED);
                    throw new UserNotFoundException("Given Email or Username is not valid.");
                }
            }

            if (user.getRegistered().equals(PermissionType.NO)) {
                AuditLogger.log(user.getUserName(), ip, FORGOT_PASSWORD, "User not registered", STATUS_FAILED);
                throw new SomethingWentWrongException("User not registered yet.");
            }
            String encrypt = encryptUserId(user.getId(), user.getUserName(), ip);

            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("name", user.getFirstName() + " " + user.getLastName());
            placeholders.put("link", forgotPasswordFrontendUrl + encrypt);

            Context context = new Context();
            context.setVariables(placeholders);

            // Process Thymeleaf template and send mail
            String htmlContent = templateEngine.process("forgetPassword.html", context);
            emailService.sendHtml(user.getEmail(), RESET_PASSWORD, htmlContent);

            ResetPassword resetPasswordExist = resetPasswordRepository.findByEncryptedUserIdOrderByCreatedAtDesc(encrypt);
            if (resetPasswordExist != null && resetPasswordExist.getId() != null) {
                resetPasswordExist.setExpiresAt(LocalDateTime.now().plusMinutes(2));
                resetPasswordExist.setApiStatus(ResetPasswordStatus.ACTIVE);
                this.resetPasswordRepository.save(resetPasswordExist);
            } else {
                ResetPassword resetPassword = new ResetPassword();
                resetPassword.setUserId(user.getId());
                resetPassword.setEncryptedUserId(encrypt);
                resetPassword.setExpiresAt(LocalDateTime.now().plusMinutes(2));
                resetPassword.setApiStatus(ResetPasswordStatus.ACTIVE);
                this.resetPasswordRepository.save(resetPassword);
            }

            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage("Reset password mail sent.");

            AuditLogger.log(user.getUserName(), ip, "ForgotPassword", "Mail sent successfully", "SUCCESS");

        } catch (UserNotFoundException | SomethingWentWrongException e) {
            // Already logged above
            throw e;
        } catch (RuntimeException e) {
            AuditLogger.log(user != null ? user.getUserName() :loginDto.getEmail(), ip, FORGOT_PASSWORD, "Email send failed", STATUS_FAILED);
            logger.error("Forgot-password mail failed", e);
            throw new ForgotPasswordEmailException("Failed to send email, please try again.");
        }

        return ResponseEntity.ok(apiResponse);
    }

    private String encryptUserId(UUID userId, String userName, String ip) {
        try {
            return aesUtil.encrypt(userId.toString());
        } catch (GeneralSecurityException e) {
            AuditLogger.log(userName, ip, FORGOT_PASSWORD, "Encryption failed", STATUS_FAILED);
            logger.error("Encryption failed for user {}", userName, e);
            // Never fall back to the plain user id in a reset link
            throw new ForgotPasswordEmailException("Failed to generate reset link, please try again.");
        }
    }

    @Override
    public User resetPassword(ResetPasswordDto resetPasswordDto) {
        String ip = request.getRemoteAddr();
        ResetPassword resetPassword = resetPasswordRepository.findByEncryptedUserIdOrderByCreatedAtDesc(resetPasswordDto.getEncryptedUserId());
        UUID userId = null;
        if (resetPassword == null) {
            try {
                resetPasswordDto.setEncryptedUserId(aesUtil.decrypt(resetPasswordDto.getEncryptedUserId()));
                userId = UUID.fromString(resetPasswordDto.getEncryptedUserId());
            } catch (GeneralSecurityException | RuntimeException e) {
                AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "failed to decrypt" + e, STATUS_FAILED);
            }
        } else {
            userId = resetPassword.getUserId();
        }
        if (userId == null) {
            AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "Invalid session, Please try again.", STATUS_FAILED);
            throw new UserNotFoundException("Invalid session, Please try again.");
        }
        if (resetPassword == null
                || !(resetPassword.getApiStatus().equals(ResetPasswordStatus.ACTIVE)
                || resetPassword.getApiStatus().equals(ResetPasswordStatus.VIEWED))) {
            AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "Session expired, Please try again.", STATUS_FAILED);
            throw new UserNotFoundException("Session expired, Please try again.");
        }
        if (resetPassword.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetPassword.setApiStatus(ResetPasswordStatus.EXPIRED);
            resetPasswordRepository.save(resetPassword);
            AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "Session time out, Please try again.", STATUS_FAILED);
            throw new UserNotFoundException("Session time out, Please try again.");
        }
        User user = userRepository.findByIdAndIsActive(userId, true);
        if (user == null) {
            AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "Invalid user.", STATUS_FAILED);
            throw new UserNotFoundException("Invalid user.");
        }
        if (user.getRegistered().equals(PermissionType.NO)) {
            AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "User not registered yet.", STATUS_FAILED);
            throw new SomethingWentWrongException("User not registered yet.");
        }
        NewRegisterDto newRegisterDto = new NewRegisterDto();
        newRegisterDto.setPassword(resetPasswordDto.getPassword());
        newRegisterDto.setConfirmPassword(resetPasswordDto.getConfirmPassword());
        newRegisterDto.setEmailId(resetPasswordDto.getEmail());
        this.keyclockUserService.updateUserForRegister(user.getKeyclockUserId(), user, newRegisterDto);
        resetPassword.setApiStatus(ResetPasswordStatus.COMPLETED);
        resetPasswordRepository.save(resetPassword);
        AuditLogger.log(resetPasswordDto.getEmail(), ip, RESET_PASSWORD, "Password has been reset successfully", STATUS_FAILED);
        return user;
    }

    @Override
    public TokenDto refreshToken(String refreshToken) {
        TokenDto tokenDto = this.keyclockUserService.refreshToken(refreshToken);
        if (tokenDto.getValid().equalsIgnoreCase(STATUS_ERROR)) {
            throw new SomethingWentWrongException(tokenDto.getMessage());
        }
        return tokenDto;
    }


    @Override
    public UserEncryptedReturnDto getUserByEncrypted(String id) {
        ResetPassword resetPassword = resetPasswordRepository.findByEncryptedUserIdOrderByCreatedAtDesc(id);
        UUID userId = null;
        if (resetPassword == null) {
            try {
                id = aesUtil.decrypt(id);
                userId = UUID.fromString(id);
            } catch (GeneralSecurityException | RuntimeException e) {
                logger.error("Failed to decrypt", e);
            }
        } else {
            userId = resetPassword.getUserId();
        }
        if (userId == null) {
            throw new UserNotFoundException("Invalid session, Please try again.");
        }
        if (resetPassword == null
                || !(resetPassword.getApiStatus().equals(ResetPasswordStatus.ACTIVE)
                || resetPassword.getApiStatus().equals(ResetPasswordStatus.VIEWED))) {
            throw new UserNotFoundException("Session expired, Please try again.");
        }
        if (resetPassword.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetPassword.setApiStatus(ResetPasswordStatus.EXPIRED);
            resetPasswordRepository.save(resetPassword);
            throw new UserNotFoundException("Session time out, Please try again.");
        }
        User user = userRepository.findByIdAndIsActive(userId, true);
        if (user == null) {
            throw new UserNotFoundException("Invalid user.");
        }
        if (user.getRegistered().equals(PermissionType.NO)) {
            throw new SomethingWentWrongException("User not registered yet.");
        }
        UserEncryptedReturnDto userEncryptedReturnDto = new UserEncryptedReturnDto();
        userEncryptedReturnDto.setEmail(user.getEmail());
        userEncryptedReturnDto.setUserName(user.getUserName());
        userEncryptedReturnDto.setId(user.getId());
        resetPassword.setApiStatus(ResetPasswordStatus.VIEWED);
        resetPasswordRepository.save(resetPassword);
        return userEncryptedReturnDto;
    }

    @Override
    public ResponseEntity<ApiResponse<JsonObject>> deleteUser(UUID id) {
        User user = userRepository.findByIdAndIsActive(id, true);
        if (user == null) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        try {
            ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
            if (user.getUserType() == UserType.CUSTOMER) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new SomethingWentWrongException("No JWT token received from gateway");
                }
                String token = authHeader.substring(7);

                // Use DTO directly for validation call
                ApiResponse<CustomerReturnDto> customerExists = customerServiceWebClient.getCustomerData(token, user.getId()).block();
                if (customerExists == null) {
                    throw new SomethingWentWrongException("Failed to fetch customer data");
                }
                if (customerExists.getStatus().equalsIgnoreCase(STATUS_ERROR)) {
                    apiResponse.setStatus(STATUS_ERROR);
                    apiResponse.setMessage(customerExists.getMessage());
                } else {
                    this.keyclockUserService.deleteUser(user.getKeyclockUserId());
                    userRepository.deleteByRoleId(user.getId());
                    userRepository.delete(user);
                    apiResponse.setStatus(STATUS_SUCCESS);
                    apiResponse.setMessage(USER_DELETED);
                }
            } else {
                this.keyclockUserService.deleteUser(user.getKeyclockUserId());
                userRepository.deleteByRoleId(user.getId());
                userRepository.delete(user);
                apiResponse.setStatus(STATUS_SUCCESS);
                apiResponse.setMessage(USER_DELETED);
            }
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            throw new DeleteUserFailedException(USER_DELETE_FAILED);
        }
    }

    /**
     * Authenticates a user using the provided login credentials.
     *
     * @param loginDto the login credentials (email and password)
     * @return LoginReturnDto containing authenticated user details
     * @throws UserNotFoundException       if no active user is found with the provided email
     * @throws SomethingWentWrongException if token generation or decoding fails
     */
    @Override
    public LoginReturnDto login(LoginDto loginDto) {
        String ip = request.getRemoteAddr();
        User userEmail = this.userRepository.findOneByEmailAndIsActive(loginDto.getEmail(), true);
        if (userEmail == null) {
            AuditLogger.log(loginDto.getEmail(), ip, USER_LOGIN, "Email Id doesn't match", STATUS_FAILED);
            throw new UserNotFoundException("Email Id doesn't match.");
        }

        if (userEmail.getRegistered().equals(PermissionType.NO)) {
            AuditLogger.log(loginDto.getEmail(), ip, USER_LOGIN, "User not registered yet", STATUS_FAILED);
            throw new SomethingWentWrongException("User not registered yet.");
        }
        TokenDto keyClock = this.keyclockUserService.login(loginDto);
        if (keyClock.getValid().equalsIgnoreCase(STATUS_ERROR)) {
            AuditLogger.log(loginDto.getEmail(), ip, USER_LOGIN, "Keyclock authentication failed - "+keyClock.getMessage(), STATUS_FAILED);
            throw new SomethingWentWrongException(keyClock.getMessage());
        }
        LoginReturnDto response = this.keyclockUserService.decode(keyClock);
        response.setId(userEmail.getId());
        Role role = this.roleRepository.findOneByNameAndIsActive(response.getRole(), true);
        List<SidenavResponseDto> sideNav = this.iPrivilegeService.sideNavNew(role.getId());
        response.setRoleId(role.getId());
        if (sideNav != null) {
            response.setSidenav(sideNav);
        }
        if (response.getFullName().isBlank() || response.getUserName().isEmpty()) {
            AuditLogger.log(loginDto.getEmail(), ip, USER_LOGIN, "Token decode failed", STATUS_FAILED);
            throw new SomethingWentWrongException("Token decode failed.");
        }
        AuditLogger.log(loginDto.getEmail(), ip, USER_LOGIN, "Sucessfully logged in", "SUCCESS");
        return response;
    }

    @Override
    public User registerNew(NewRegisterDto newRegisterDto) {
        User user = this.userRepository.findOneByEmailAndIsActive(newRegisterDto.getEmailId(), true);
        if (user == null) {
            throw new UserNotFoundException("Given email-id is not valid.");
        }
        if (!newRegisterDto.getPassword().equals(newRegisterDto.getConfirmPassword())) {
            throw new SomethingWentWrongException("Password and confirm password mismatch.");
        }
        if (user.getRegistered().equals(PermissionType.YES)) {
            throw new SomethingWentWrongException("User already registered.");
        }
        this.keyclockUserService.updateUserForRegister(user.getKeyclockUserId(), user, newRegisterDto);
        user.setRegistered(PermissionType.YES);
        this.userRepository.save(user);
        return user;
    }

    @Override
    public boolean checkCustomerExists(UserDetailsDto userDetailsDto) {
        // Check email exists
        if (userDetailsDto.getEmail() != null && !userDetailsDto.getEmail().isEmpty()) {
            User user = this.userRepository.findOneByEmailAndIsActive(userDetailsDto.getEmail(), true);
            if (user != null) {
                return false;
            }
        }

        // Check phone number exists
        if (userDetailsDto.getPhoneNumber() != null && !userDetailsDto.getPhoneNumber().isEmpty()) {
            User user = this.userRepository.findOneByPhoneNumberAndIsActive(userDetailsDto.getPhoneNumber(), true);
            if (user != null) {
                return false;
            }
        }

        return true; // ✅ No conflicts found
    }

    private Long generateNextCustomerNumber() {
        Query query = entityManager.createNativeQuery("SELECT nextval('user_schema.users_no_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public List<User> getUsersCount() {
        return this.userRepository.findAllByIsActive(true);
    }

    @Override
    public boolean logout(String token) {
        try {
            return this.keyclockUserService.logout(token);
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to logout.");
        }

    }

    public void rollbackUserCreation(UUID userId) {
        try {
            logger.info("Starting user rollback for User Id: {}", userId);
            this.deleteUser(userId);
            logger.info("Successfully rolled back user: {}", userId);

        } catch (RuntimeException e) {
            logger.error("Error rolling back user creation for for User Id: {}", userId);
            throw new RollbackFailureException("User rollback failed", e);
        }
    }

    public void deleteUserRoleback(UUID id) {
        User user = userRepository.findByIdAndIsActive(id, true);
        this.keyclockUserService.deleteUser(user.getKeyclockUserId());
        userRepository.deleteByRoleId(user.getId());
        userRepository.delete(user);
    }







    @Transactional
    public void rollbackUserByEmail(String email) {
        try {
            logger.info("🎯 ROLLBACK STARTED FOR: {}", email);

            Optional<User> userOptional = userRepository.findByEmailId(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UUID userId = user.getId();
                String deleteRoles = "DELETE FROM user_schema.user_roles WHERE user_id = ?";
                int rolesDeleted = entityManager.createNativeQuery(deleteRoles)
                        .setParameter(1, userId)
                        .executeUpdate();

                userRepository.delete(user);
                userRepository.flush();

                logger.info("✅ ROLLBACK SUCCESS - Deleted user: {}, roles: {}", email, rolesDeleted);
            } else {
                logger.warn("⚠️ User not found: {}", email);
            }
        }
            catch (RuntimeException e) {
                throw new UserRollbackException("Rollback failed for userId " , e);
            }

        }

    public void rollbackUserByPhoneNumber(String phoneNumber) {
        try {
            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
            if (userOptional.isPresent()) {
                userRepository.delete(userOptional.get());
                logger.info("✅ Rolled back user with phone: {}", phoneNumber);
            } else {
                logger.warn("User not found with phone: {}", phoneNumber);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to rollback user with phone: {}", phoneNumber, e);
        }
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    public String getCurrentAuthenticatedUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !(authentication instanceof AnonymousAuthenticationToken)) {

                String usernameFromAuth = authentication.getName();

                // Since we're in user service, we can directly use UserRepository
                User authenticatedUser = userRepository.findByUserName(usernameFromAuth);
                if (authenticatedUser == null || authenticatedUser.getId() == null) {
                    authenticatedUser = userRepository.findByEmail(usernameFromAuth);
                }

                if (authenticatedUser != null && authenticatedUser.getId() != null) {
                    return authenticatedUser.getUserName(); // Use actual username
                } else {
                    return usernameFromAuth; // Fallback to security context name
                }
            }
        } catch (RuntimeException e) {
            logger.error("Exception in getCurrentAuthenticatedUsername", e);
        }
        return "system"; // Default fallback
    }
}
