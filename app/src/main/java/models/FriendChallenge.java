package models;

/**
 * Created by Zach on 2/20/2018.
 */

public class FriendChallenge {
    public int numFriends;
    public String title;
    public int xp;
    public int id;

    public FriendChallenge(){

    }
    public FriendChallenge(int numFriends, String title, int xp, int id){
        this.numFriends = numFriends;
        this.title = title;
        this.xp = xp;
        this.id = id;

    }

    public int getNumFriends(){
        return this.numFriends;
    }
    public String getTitle(){
        return this.title;
    }
    public int getXp(){
        return this.xp;
    }
    public int getId(){return this.id;}


}
