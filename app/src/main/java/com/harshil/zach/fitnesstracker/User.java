package com.harshil.zach.fitnesstracker;

/**
 * Created by Zach on 2/23/2018.
 */

public class User {
    public String email;
    public String name;
    public int rank;
    public  int xp;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.rank = 1;
        this.xp = 0;
    }
    public String getName(){
        return this.name;
    }
    public String getEmail(){
        return this.email;
    }
    public int getRank(){
        return this.rank;
    }
    public int xp(){
        return this.xp;
    }

}
