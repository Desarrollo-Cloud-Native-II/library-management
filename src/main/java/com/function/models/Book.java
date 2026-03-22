package com.function.models;

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

    // Empty constructor
    public Book() {
    }

    // Constructor with all parameters
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

    // Getters and Setters
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

    // Enum for book status
    public enum BookStatus {
        AVAILABLE,
        BORROWED,
        RESERVED
    }
}
