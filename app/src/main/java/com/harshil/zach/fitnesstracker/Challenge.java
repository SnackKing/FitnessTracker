package com.harshil.zach.fitnesstracker;

/**
 * Created by Zach on 2/20/2018.
 */

public class Challenge {
    public int days;
    public int numsteps;
    public String title;
    public int xp;
    public int id;

    public Challenge(){

    }
    public Challenge(int days, int numsteps, String title, int xp, int id){
        this.days = days;
        this.numsteps = numsteps;
        this.title = title;
        this.xp = xp;
        this.id = id;
    }

    public int getDays(){
        return this.days;
    }
    public int getNumsteps(){
        return this.numsteps;
    }
    public String getTitle(){
        return this.title;
    }
    public int getXp(){
        return this.xp;
    }
    public int getId(){return this.id;};


}
