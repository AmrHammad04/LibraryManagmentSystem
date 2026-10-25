package com.library.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.library.demo.validation.ValidPublicationYear;
@Entity
@Table(name = "library_item")
@Inheritance(strategy = InheritanceType.JOINED)
public class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank(message = "Title is required")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Author or publisher is required")
    @Column(name = "author_publisher")
    private String author;

    @NotNull(message = "Publication year is required")
    @Min(value = 1, message = "Publication year must be positive")
    @ValidPublicationYear
    @Column(name = "Year_of_Publication")
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
