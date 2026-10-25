package com.library.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "magazine")
public class Magazine extends LibraryItem {

    @NotNull(message = "Issue number is required")
    @Min(value = 1, message = "Issue number must be at least 1")
    private Integer issueNumber;

    @NotBlank(message = "Category is required")
    private String category;

    public Magazine() {
    }

    public Magazine(
            String title,
            String author,
            Integer yearOfPublication,
            Boolean availability,
            Integer issueNumber,
            String category) {
        super(title, author, yearOfPublication, availability);
        this.issueNumber = issueNumber;
        this.category = category;
    }

    public Integer getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(Integer issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}