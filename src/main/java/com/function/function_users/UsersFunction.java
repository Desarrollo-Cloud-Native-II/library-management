package com.function.function_users;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.models.User;
import com.function.repository.UserRepository;
import com.google.gson.Gson;

import java.util.Optional;

public class UsersFunction {
    private final UserRepository userRepository = new UserRepository();
    private final Gson gson = new Gson();

    /**
     * Get all users
     * GET /api/users
     */
    @FunctionName("GetAllUsers")
    public HttpResponseMessage getAllUsers(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting all users");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(userRepository.findAll()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting users: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting users: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @FunctionName("GetUserById")
    public HttpResponseMessage getUserById(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Getting user with ID: " + id);

        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(user.get()))
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + id)
                    .build();
        }
    }

    /**
     * Get user by RUT
     * GET /api/users/rut/{rut}
     */
    @FunctionName("GetUserByRut")
    public HttpResponseMessage getUserByRut(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/rut/{rut}") HttpRequestMessage<Optional<String>> request,
            @BindingName("rut") String rut,
            final ExecutionContext context) {

        context.getLogger().info("Getting user with RUT: " + rut);

        Optional<User> user = userRepository.findByRut(rut);

        if (user.isPresent()) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(user.get()))
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("User not found with RUT: " + rut)
                    .build();
        }
    }

    /**
     * Get active users
     * GET /api/users/active
     */
    @FunctionName("GetActiveUsers")
    public HttpResponseMessage getActiveUsers(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/status/active") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Getting active users");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(userRepository.findActiveUsers()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting active users: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting active users: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Create new user
     * POST /api/users
     */
    @FunctionName("CreateUser")
    public HttpResponseMessage createUser(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new user");

        try {
            String body = request.getBody().orElse("");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }

            User user = gson.fromJson(body, User.class);

            // Validate required fields
            if (user.getRut() == null || user.getRut().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("RUT is required")
                        .build();
            }

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Email is required")
                        .build();
            }

            // Check if RUT already exists
            Optional<User> existingUser = userRepository.findByRut(user.getRut());
            if (existingUser.isPresent()) {
                return request.createResponseBuilder(HttpStatus.CONFLICT)
                        .body("User with RUT " + user.getRut() + " already exists")
                        .build();
            }

            // Generate ID if not provided
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(userRepository.getNextId());
            }

            // Set default active status if not provided
            if (!user.isActive()) {
                user.setActive(true);
            }

            User savedUser = userRepository.save(user);

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(savedUser))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error creating user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Update user
     * PUT /api/users/{id}
     */
    @FunctionName("UpdateUser")
    public HttpResponseMessage updateUser(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating user with ID: " + id);

        try {
            // Check if user exists
            Optional<User> existingUser = userRepository.findById(id);
            if (!existingUser.isPresent()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("User not found with ID: " + id)
                        .build();
            }

            String body = request.getBody().orElse("");

            if (body.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is required")
                        .build();
            }

            User user = gson.fromJson(body, User.class);
            user.setId(id); // Ensure ID from URL is used

            User updatedUser = userRepository.save(user);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(updatedUser))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error updating user: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @FunctionName("DeleteUser")
    public HttpResponseMessage deleteUser(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.DELETE }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting user with ID: " + id);

        boolean deleted = userRepository.deleteById(id);

        if (deleted) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("User deleted successfully")
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("User not found with ID: " + id)
                    .build();
        }
    }
}
