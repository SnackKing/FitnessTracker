package models;

/**
 * Created by Zach on 3/10/2018.
 */
public class Rank {
    public int xp;
    public int level;


    public Rank(){

    }
    public Rank(int level, int xp){
        this.level = level;
        this.xp = xp;
    }


    public int level(){
        return this.level;
    }
    public int getXp(){
        return this.xp;
    }



}
