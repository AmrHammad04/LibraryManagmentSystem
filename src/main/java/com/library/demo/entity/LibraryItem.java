package com.library.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "library_item")
@Inheritance(strategy = InheritanceType.JOINED)
public class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "author_publisher")
    private String author;

    @Column(name = "year_of_publication")
    private Integer yearOfPublication;

    @Column(name = "availability")
    private Boolean availability;


    public LibraryItem() {
    }

    public LibraryItem(
            String title,
            String author,
            Integer yearOfPublication,
            Boolean availability) {

        this.title = title;
        this.author = author;
        this.yearOfPublication = yearOfPublication;
        this.availability = availability;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
                
    public Integer getYearOfPublication() {
        return yearOfPublication;
    }

    public void setYearOfPublication(Integer yearOfPublication) {
        this.yearOfPublication = yearOfPublication;
    }
    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }


    @Override
    public String toString() {
        return "Library Item{" +
            ", Title: '" + title + '\'' +
            ", Author/Publisher: '" + author + '\'' +
            ", Year of Pubication: '" + yearOfPublication + '\'' +
            ", Avilability: '" + availability + '\'' +
            "Library Item ID: " + id +
            '}';
    }
}
