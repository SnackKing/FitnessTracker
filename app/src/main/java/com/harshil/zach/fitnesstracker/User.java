package com.harshil.zach.fitnesstracker;

/**
 * Created by Zach on 2/23/2018.
 */

public class User {
    public String email;
    public String name;
    public int rank;
    public  int xp;
    public int runRank;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.rank = 1;
        this.xp = 0;
        this.runRank = 1;
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

    public int xp(){
        return this.xp;
    }

}
