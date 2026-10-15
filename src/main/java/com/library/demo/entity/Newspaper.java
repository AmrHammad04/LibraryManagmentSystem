package com.library.demo.entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "newspaper")
public class Newspaper extends LibraryItem {

    private Date publicationDate;
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