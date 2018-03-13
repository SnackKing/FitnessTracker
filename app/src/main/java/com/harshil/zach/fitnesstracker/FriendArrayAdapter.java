package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by Zach on 2/21/2018.
 */
public class FriendArrayAdapter extends ArrayAdapter<User>
{
    private Context mContext;
    List<User> friendList;


    public FriendArrayAdapter(Context context, List<User> list)
    {
        super(context,0,list);
        friendList = list;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;

        if (convertView == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
        }
        User currentFriend = friendList.get(position);


        TextView name = (TextView) listItem.findViewById(R.id.name);
        name.setText(currentFriend.getName());


        TextView email = (TextView) listItem.findViewById(R.id.email);
        email.setText(currentFriend.getEmail());

        TextView rank = (TextView) listItem.findViewById(R.id.rank);
        rank.setText("Rank " + Integer.toString(currentFriend.getRank()));
        return listItem;



    }

    static class ViewHolder
    {

        TextView tv;
    }
}