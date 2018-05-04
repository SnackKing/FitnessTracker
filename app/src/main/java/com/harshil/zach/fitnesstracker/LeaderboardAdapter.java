package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zach on 2/21/2018.
 */
public class LeaderboardAdapter extends BaseAdapter
{
    private  ArrayList mData;

    public LeaderboardAdapter(Map<String,String> map)
    {
        mData = new ArrayList();
        mData.addAll(map.entrySet());

    }

    @Override
    public int getCount() {
        return mData.size();

    }


    @Override
    public Map.Entry<String, String> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, String> item = getItem(position);
        TextView pos = result.findViewById(R.id.position);
        pos.setText(Integer.toString(position+1));
        TextView name = result.findViewById(R.id.name);
        name.setText(item.getKey());
        TextView time = result.findViewById(R.id.time);
        time.setText(item.getValue());


        return result;



    }

    static class ViewHolder
    {

        TextView tv;
    }
}