package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "magazine")
public class Magazine extends LibraryItem {

    private int issueNumber;
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