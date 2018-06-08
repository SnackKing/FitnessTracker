package models;

import java.io.Serializable;

/**
 * Created by harshil on 3/24/18.
 */

public class RunningChallenge implements Serializable {
    public String description;
    public double distance;
    public String time;
    public int xp;
    public int id;

    public RunningChallenge(){

    }
    public RunningChallenge(String description, double distance, String time, int xp, int id){
        this.description = description;
        this.distance = distance;
        this.time = time;
        this.xp = xp;
        this.id = id;
    }

    public String getDescription(){
        return this.description;
    }
    public double getDistance(){
        return this.distance;
    }
    public String getTime(){
        return this.time;
    }
    public int getTimeMinutes(String Time){
        int minutes = 0;
        int i = 0;
        StringBuilder minuteString = new StringBuilder();
        while (Time.charAt(i) != ':'){
            minuteString.append(Time.charAt(i));
            i = i + 1;
        }
        minutes = Integer.valueOf(minuteString.toString());
        return minutes;
    }

    public int getTimeSeconds(String Time){
        int seconds = 0;
        int i = 0;
        StringBuilder secondString = new StringBuilder();
        while (Time.charAt(i) != ':'){
            i = i + 1;
        }
        i = i + 1;
        while(i < Time.length()){
            secondString.append(Time.charAt(i));
            i = i + 1;
        }
        seconds = Integer.valueOf(secondString.toString());
        return seconds;

    }
    public int getXp(){
        return this.xp;
    }
    public int getId(){return this.id;};
}
