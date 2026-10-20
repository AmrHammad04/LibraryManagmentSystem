package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "magazine")
public class Magazine extends LibraryItem {

    @Min(value = 1, message = "Issue number must be at least 1")
    private int issueNumber;

    @NotBlank(message = "Category is required")
    private String category;
    

    public Magazine() {}

    public Magazine(String title, String author, Integer yearOfPublication,Boolean availability, int issueNumber, String category) {
        super(title, author, yearOfPublication,availability);
        this.issueNumber = issueNumber;
        this.category = category;
    }

    public String getCategory() {
         return category; 
        }
    
    public void setCategory(String category) {
         this.category = category; 
        }
    
    public Integer getIssueNumber() {
         return issueNumber; 
        }

    public void setIssueNumber(int issueNumber) {
         this.issueNumber = issueNumber; 
        }
}