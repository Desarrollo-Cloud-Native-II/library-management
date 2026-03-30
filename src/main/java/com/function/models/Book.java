package com.function.models;

/**
 * Representa un libro en el catálogo de la biblioteca.
 * Contiene información bibliográfica completa y estado de disponibilidad.
 */
public class Book {

    private String id;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private String language;
    private String isbn;
    private String genre;
    private String description;
    private BookStatus status;

    /**
     * Constructor vacío para inicialización por defecto.
     */
    public Book() {
    }

    /**
     * Constructor con todos los parámetros.
     * 
     * @param id              identificador único del libro
     * @param title           título del libro
     * @param author          autor del libro
     * @param publisher       editorial del libro
     * @param publicationYear año de publicación
     * @param language        idioma del libro
     * @param isbn            código ISBN del libro
     * @param genre           género literario
     * @param description     descripción o sinopsis del libro
     * @param status          estado de disponibilidad del libro
     */
    public Book(String id, String title, String author, String publisher,
            int publicationYear, String language, String isbn,
            String genre, String description, BookStatus status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.language = language;
        this.isbn = isbn;
        this.genre = genre;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    /**
     * Estados posibles de un libro en el sistema.
     */
    public enum BookStatus {
        AVAILABLE,
        BORROWED,
        RESERVED
    }
}
