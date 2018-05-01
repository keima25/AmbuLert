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
    String type;


    Users(){

    }

    public Users(String id, String email, String firstname, String lastname, String phone, String type) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.type = type;
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

    public String getType() {return type;}
}
