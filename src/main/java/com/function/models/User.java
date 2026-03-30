package com.function.models;

/**
 * Representa un usuario del sistema de biblioteca.
 * Contiene información personal y estado de activación del usuario.
 */
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String rut;
    private String email;
    private boolean active;

    /**
     * Constructor vacío para inicialización por defecto.
     */
    public User() {
    }

    /**
     * Constructor con todos los parámetros.
     * 
     * @param id        identificador único del usuario
     * @param firstName nombre del usuario
     * @param lastName  apellido del usuario
     * @param rut       RUT del usuario (formato chileno)
     * @param email     correo electrónico del usuario
     * @param active    estado de activación del usuario
     */
    public User(String id, String firstName, String lastName, String rut, String email, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.rut = rut;
        this.email = email;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
