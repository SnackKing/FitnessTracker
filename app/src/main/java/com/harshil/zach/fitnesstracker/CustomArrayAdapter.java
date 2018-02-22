package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Zach on 2/21/2018.
 */
public class CustomArrayAdapter extends ArrayAdapter<Challenge>
{
    private Context mContext;
    List<Challenge> challengeList;
    public CustomArrayAdapter(Context context, List<Challenge> list)
    {
        super(context,0,list);
        challengeList = list;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;

        if (convertView == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.message, parent, false);
        }
        Challenge currentChallenge = challengeList.get(position);

        ImageView image = (ImageView) listItem.findViewById(R.id.walker);
        image.setImageResource(R.drawable.ic_walk);

        TextView name = (TextView) listItem.findViewById(R.id.challengeDescription);
        name.setText(currentChallenge.getTitle());

        TextView xp = (TextView) listItem.findViewById(R.id.xp);
        xp.setText(Integer.toString(currentChallenge.getXp()));
        return listItem;



    }

    static class ViewHolder
    {

        TextView tv;
    }
}