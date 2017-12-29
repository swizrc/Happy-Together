package com.itschner.sam.happytogether;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User{
    public String Fname; //First Name
    public String Lname; //Last Name
    public String userID; //User's unique ID
    public String email; //User's email
    public Map<String,String> pending; //List of pending user emails
    public Map<String,String> partners; //List of users this user has been with
    public boolean status; //Whether this user is currently in a relationship

    public User(){}

    public User(String name, String userID, String email){
        String[] FullName = name.split(" ");
        StringBuilder LastName = new StringBuilder();
        this.Fname = FullName[0];
        for(int i=1;i < FullName.length;i++){
            LastName.append(FullName[i]);
        }
        this.Lname = LastName.toString();
        this.status = false;
        this.partners = new HashMap<>();
        this.userID = userID;
        this.pending = new HashMap<>();
        this.email = email;
    }

    public String getUserID(){
        return this.userID;
    }

}



