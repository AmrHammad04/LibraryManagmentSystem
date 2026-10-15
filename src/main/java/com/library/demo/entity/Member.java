package com.library.demo.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "Name")
    private String name;

    @Column(name = "PhoneNumber")
    private String phone;

    @Column(name = "Email")
    private String email;

    public Member() {
    }

    public Member(
            String name,
            String phone,
            String email) {

        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Member{" +
            ", Name: '" + name + '\'' +
            ", email: '" + email + '\'' +
            ", Phone Number: '" + phone + '\'' +
            "Membership ID: " + id +
            '}';
    }
}
