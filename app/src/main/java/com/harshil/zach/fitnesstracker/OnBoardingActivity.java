package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Slide;
import android.util.AndroidException;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class OnBoardingActivity extends AppCompatActivity {
    ViewPager mViewPager;
    private LinearLayout dotsLayout;
    Button next;
    Button back;
    private TextView dots[];
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        next = findViewById(R.id.next);
        back = findViewById(R.id.back);
        dotsLayout = findViewById(R.id.BottomDots);
        sliderAdapter = new SliderAdapter(this);
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addDotsIndicator(position);
                currentPage = position;
                if(position == 0){
                    next.setEnabled(true);
                    back.setEnabled(false);
                    back.setVisibility(View.INVISIBLE);
                    next.setText("Next");
                    back.setText("");
                }
                else if(position == dots.length -1){
                    next.setEnabled(true);
                    back.setEnabled(true);
                    back.setVisibility(View.VISIBLE);
                    next.setText("Finish");
                    back.setText("Back");
                }
                else{
                    next.setEnabled(true);
                    back.setEnabled(true);
                    back.setVisibility(View.VISIBLE);
                    next.setText("Next");
                    back.setText("Back");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        mViewPager.addOnPageChangeListener(viewListener);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPage == dots.length-1){
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor= sharedPreferences.edit();
                    editor.putBoolean("completedBoarding",true);
                    editor.apply();

                    Intent intent = new Intent(getApplicationContext(), MainAndRunningTabsScreen.class);
                    startActivity(intent);
                }
                else {
                    mViewPager.setCurrentItem(currentPage + 1);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem((currentPage-1));
            }
        });

    }
    public void addDotsIndicator(int position){
        dots = new TextView[3];
        dotsLayout.removeAllViews();
        for(int i = 0; i < dots.length;i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(Color.WHITE);
            dotsLayout.addView(dots[i]);

        }
        dots[position].setTextColor(Color.BLACK);
    }

//    @Override
//    protected void onFinishFragment() {
//        super.onFinishFragment();
//        // User has seen OnboardingFragment, so mark our SharedPreferences
//        // flag as completed so that we don't show our OnboardingFragment
//        // the next time the user launches the app.
//        SharedPreferences.Editor sharedPreferencesEditor =
//                PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
//        sharedPreferencesEditor.putBoolean(
//                COMPLETED_ONBOARDING_PREF_NAME, true);
//        sharedPreferencesEditor.apply();
//    }

}
