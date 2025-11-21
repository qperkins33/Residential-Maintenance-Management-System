package com.maintenance.models;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String username,
                 String password,
                 String firstName,
                 String lastName,
                 String email,
                 String phoneNumber) {
        super(username, password, firstName, lastName, email, phoneNumber);
    }

    public String getDisplayRole() {
        return "Admin";
    }
}
