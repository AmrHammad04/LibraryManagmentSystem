package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "books")
public class Book extends LibraryItem {

    @NotBlank(message ="ISBN is required")
    private String isbn;

    @Min(value =1,message = "Pages must be at least 1")
    private int numberOfPages;

    public Book() {}

    public Book(String title, String author, int yearOfPublication,Boolean availability, String isbn, int numberOfPages) {
        super(title, author, yearOfPublication,availability);
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

    public void setNumberOfPages(int numberOfPages) {
         this.numberOfPages = numberOfPages; 
        }
}