package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.harshil.zach.fitnesstracker.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zach on 2/21/2018.
 */
public class NotificationAdapter extends BaseAdapter
{
    private  ArrayList mData;

    public NotificationAdapter(Map<String,String> map)
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
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, String> item = getItem(position);
        TextView date = result.findViewById(R.id.notificationDate);
        date.setText(item.getKey());
        TextView text = result.findViewById(R.id.notificationText);
        text.setText(item.getValue());
        return result;



    }

    static class ViewHolder
    {

        TextView tv;
    }
}