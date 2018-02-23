package com.example.keima.ambulife;

/**
 * Created by Steven on 23/02/2018.
 */

public class Users {

    String id;
    String email;
    String firstname;
    String lastname;
    String phone;


    Users(){

    }

    public Users(String id, String email, String firstname, String lastname, String phone) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhone() {
        return phone;
    }
}
