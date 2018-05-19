package com.harshil.zach.fitnesstracker;

/**
 * Created by Zach on 2/20/2018.
 */

public class Challenge {
    public int days;
    public int numSteps;
    public String title;
    public int xp;
    public int id;
    public String type;

    public Challenge(){

    }
    public Challenge(int days, int numSteps, String title, int xp, int id,String type){
        this.days = days;
        this.numSteps = numSteps;
        this.title = title;
        this.xp = xp;
        this.id = id;
        this.type = type;

    }

    public int getDays(){
        return this.days;
    }
    public int getNumSteps(){
        return this.numSteps;
    }
    public String getTitle(){
        return this.title;
    }
    public int getXp(){
        return this.xp;
    }
    public int getId(){return this.id;}
    public String getType(){return this.type;}


}
