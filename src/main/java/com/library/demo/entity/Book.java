package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "books")
public class Book extends LibraryItem {

    @NotBlank(message = "ISBN is required")
    @Pattern(
            regexp = "^(?:[0-9][ -]?){9}[0-9Xx]$|^(?:[0-9][ -]?){12}[0-9]$",
            message = "ISBN must have 10 or 13 characters after removing spaces and hyphens"
    )
    private String isbn;

    @NotNull(message = "Pages are required")
    @Min(value = 1, message = "Pages must be at least 1")
    private Integer numberOfPages;

    public Book() {
    }

    public Book(
            String title,
            String author,
            int yearOfPublication,
            Boolean availability,
            String isbn,
            Integer numberOfPages) {
        super(title, author, yearOfPublication, availability);
        this.isbn = isbn;
        this.numberOfPages = numberOfPages;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }
}