package com.function.repository;

import com.function.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static List<User> users = new ArrayList<>();

    // Initialize with static data
    static {
        users.add(new User("1", "Juan", "Pérez", "12345678-9",
                "juan.perez@email.com", true));

        users.add(new User("2", "María", "González", "23456789-0",
                "maria.gonzalez@email.com", true));

        users.add(new User("3", "Pedro", "Martínez", "34567890-1",
                "pedro.martinez@email.com", true));

        users.add(new User("4", "Ana", "López", "45678901-2",
                "ana.lopez@email.com", false));

        users.add(new User("5", "Carlos", "Rodríguez", "56789012-3",
                "carlos.rodriguez@email.com", true));
    }

    // Get all users
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    // Get user by ID
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    // Get user by RUT
    public Optional<User> findByRut(String rut) {
        return users.stream()
                .filter(user -> user.getRut().equals(rut))
                .findFirst();
    }

    // Get user by email
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    // Create or update user
    public User save(User user) {
        // Check if user already exists
        Optional<User> existing = findById(user.getId());
        if (existing.isPresent()) {
            // Update existing user
            users.remove(existing.get());
        }
        users.add(user);
        return user;
    }

    // Delete user
    public boolean deleteById(String id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    // Get active users
    public List<User> findActiveUsers() {
        return users.stream()
                .filter(User::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    // Get next ID for new users
    public String getNextId() {
        return String.valueOf(users.size() + 1);
    }
}
