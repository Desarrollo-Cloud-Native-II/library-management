package com.function.function_users;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.function.models.User;
import com.function.repository.UserRepository;
import com.google.gson.Gson;

import java.util.Optional;

/**
 * Funciones HTTP para gestionar usuarios del sistema de biblioteca.
 * Expone endpoints REST para operaciones CRUD de usuarios.
 */
public class UsersFunction {
        private final UserRepository userRepository = new UserRepository();
        private final Gson gson = new Gson();

        /**
         * Obtiene todos los usuarios del sistema.
         * 
         * @param request solicitud HTTP
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con lista de usuarios en formato JSON
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
         * Obtiene un usuario por su ID.
         * 
         * @param request solicitud HTTP
         * @param id      identificador del usuario
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con el usuario en formato JSON o error 404
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
         * Obtiene un usuario por su RUT.
         * 
         * @param request solicitud HTTP
         * @param rut     RUT del usuario
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con el usuario en formato JSON o error 404
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
         * Obtiene todos los usuarios activos.
         * 
         * @param request solicitud HTTP
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con lista de usuarios activos en formato JSON
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
         * Crea un nuevo usuario en el sistema.
         * 
         * @param request solicitud HTTP con datos del usuario en el body (JSON)
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con el usuario creado o errores de validación
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

                        Optional<User> existingUser = userRepository.findByRut(user.getRut());
                        if (existingUser.isPresent()) {
                                return request.createResponseBuilder(HttpStatus.CONFLICT)
                                                .body("User with RUT " + user.getRut() + " already exists")
                                                .build();
                        }

                        if (user.getId() == null || user.getId().isEmpty()) {
                                user.setId(userRepository.getNextId());
                        }

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
         * Actualiza un usuario existente.
         * 
         * @param request solicitud HTTP con datos actualizados del usuario (JSON)
         * @param id      identificador del usuario a actualizar
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con el usuario actualizado o error 404
         */
        @FunctionName("UpdateUser")
        public HttpResponseMessage updateUser(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.PUT }, authLevel = AuthorizationLevel.ANONYMOUS, route = "users/{id}") HttpRequestMessage<Optional<String>> request,
                        @BindingName("id") String id,
                        final ExecutionContext context) {

                context.getLogger().info("Updating user with ID: " + id);

                try {
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
                        user.setId(id);

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
         * Elimina un usuario del sistema.
         * 
         * @param request solicitud HTTP
         * @param id      identificador del usuario a eliminar
         * @param context contexto de ejecución de Azure Functions
         * @return respuesta HTTP con confirmación o error 404
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
