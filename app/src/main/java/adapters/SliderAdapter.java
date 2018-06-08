package adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.harshil.zach.fitnesstracker.R;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;
    public int[] slide_images = {R.drawable.rank_example_edit,R.drawable.friend_example_edit,R.drawable.running_example_edit};
    public String[] slide_headings = {"Level up your fitness","Find and compete with friends", "Run the leaderboards"};
    public  String[] slide_descriptions = {"", "", ""};

    public SliderAdapter(Context context){
        this.context = context;
        slide_descriptions[0] = context.getResources().getString(R.string.boardDesc1);
        slide_descriptions[1] = context.getResources().getString(R.string.boardDesc2);
        slide_descriptions[2] = context.getResources().getString(R.string.boardDesc3);

    }
    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position){
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.boarding_viewpager_item,container,false);

        ImageView image = view.findViewById(R.id.onboard_image);
        TextView header = view.findViewById(R.id.header);
        TextView desc = view.findViewById(R.id.desc);

        image.setImageResource(slide_images[position]);
        header.setText(slide_headings[position]);
        desc.setText(slide_descriptions[position]);

        container.addView(view);

        return view;


    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((RelativeLayout) object);
    }
}
