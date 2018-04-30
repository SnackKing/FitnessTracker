package com.harshil.zach.fitnesstracker;

import android.media.Image;
import android.net.Uri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Zach on 2/23/2018.
 */

public class User {
    public String email;
    public String name;
    public int rank;
    public  int xp;
    public int runRank;
    public String profile;
    public int runXp;
    public int totalSteps;
    public String signUpDate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.rank = 1;
        this.xp = 0;
        this.runRank = 1;
        this.profile = "";
        this.runXp = 0;
        this.totalSteps = 0;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = format1.format(calendar.getTime());
        this.signUpDate = formattedDate;
    }
    @Override
    public boolean equals(Object o)
    {
        boolean isEqual= false;
        if(o == this){
            return true;
        }
        if(!(o instanceof  User)){
            return false;
        }
        else{
            User u = (User) o;
            if(u.rank == this.rank && u.xp == this.xp && u.runRank == this.runRank && u.email.equals(this.email) && u.name.equals(this.name)){
                return true;
            }
        }
        return isEqual;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + email.hashCode();

        return result;
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
    public int getRunRank(){
        return this.runRank;
    }
    public String getProfile(){return this.profile;}
    public int xp(){
        return this.xp;
    }
    public int getRunXp(){
        return this.runXp;
    }
    public int totalSteps(){return this.totalSteps; };
    public String signUpDate(){return this.signUpDate; };

}
