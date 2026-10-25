package com.library.demo.entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "newspaper")
public class Newspaper extends LibraryItem {

    @NotNull(message = "Publication date is required")
    private Date publicationDate;
    @NotBlank(message = "Region is required")
    private String region;
    

    public Newspaper() {}

    public Newspaper(String title, String author, Integer yearOfPublication,Boolean availability, Date publicationDate, String region) {
        super(title, author, yearOfPublication,availability);
        this.region = region;
        this.publicationDate = publicationDate;
    }

    public String getRegion() {
         return region; 
        }
    
    public void setRegion(String region) {
         this.region = region; 
        }
    
    public Date getPublicationDate() {
         return publicationDate; 
        }

    public void setPublicationDate(Date publicationDate) {
         this.publicationDate = publicationDate; 
        }
}