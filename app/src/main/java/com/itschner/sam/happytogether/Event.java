package com.itschner.sam.happytogether;

/**
 * Created by Sam on 11/20/2017.
 */

public class Event {
    public String title;
    public float score;
    public String description;

    Event (String title,String description){
        this.title = title;
        this.score = 0;
        this.description = description;
    }

    public Event(){}
}
