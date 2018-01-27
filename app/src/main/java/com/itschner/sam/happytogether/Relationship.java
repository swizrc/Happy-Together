package com.itschner.sam.happytogether;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sam on 1/22/2018.
 */

public class Relationship {
    public Map<String,String> pending; // UserID/UserEmail
    public Map<String,String> partners; //List of users this relationship has
    public String ID;
    public String originalUserID;
    public String name;
    public Map<String,Event> events;

    Relationship(){}

    Relationship(String name,String id,String userID){
        this.name = name;
        this.ID = id;
        this.originalUserID = userID;
        this.pending = new HashMap<>();
        this.pending.put("dummy","dummy");
        this.partners = new HashMap<>();
        this.partners.put("dummy","dummy");
        this.events = new HashMap<>();
        this.events.put("dummy",new Event("dummy","dummy"));
    }
}


