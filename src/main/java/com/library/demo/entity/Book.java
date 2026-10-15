package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book extends LibraryItem {

    private String isbn;
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