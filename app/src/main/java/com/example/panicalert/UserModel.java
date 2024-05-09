package com.example.panicalert;

public class UserModel {
    private String user,phone,email;

    public String getName() {
        return user;
    }

    public void setName(String name) {
        this.user = name;
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

    public UserModel(String name) {
        this.user = user;
    }

    public UserModel(String name, String phone, String email) {
        this.user = user;
        this.phone = phone;
        this.email = email;
    }
}
