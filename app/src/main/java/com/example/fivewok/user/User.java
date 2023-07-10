package com.example.fivewok.user;

public class User {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String dob;
    private String imageUrl;


    public User() {
        // Обязательный пустой конструктор для Firebase
    }



    public User(String name,String surname, String email, String password, String dob) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.dob = dob;

    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDob() {
        return dob;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
