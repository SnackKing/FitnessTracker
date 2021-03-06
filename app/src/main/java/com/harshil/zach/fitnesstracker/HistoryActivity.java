package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import models.User;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


public class HistoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "HistorytActivity";
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    ArrayList<DataPoint> data = new ArrayList<>();
    GoogleApiClient mGoogleApiClient;
    private DrawerLayout mDrawerLayout;
    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabase;
    TextView totalSteps;
    TextView averageSteps;
    TextView recordSteps;
    Spinner spinner;
    final int WEEK = 0;
    final int MONTH = 1;
    final int ALLTIME = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        totalSteps = findViewById(R.id.totalSteps);
        averageSteps = findViewById(R.id.averageSteps);
        recordSteps = findViewById(R.id.recordSteps);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(HistoryActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(HistoryActivity.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(HistoryActivity.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_leaderboard){
                            Intent intent = new Intent(HistoryActivity.this,LeaderBoardActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
        spinner = (Spinner) findViewById(R.id.timeline);
        spinner.setOnItemSelectedListener(this);
        String[] spinnerArray = {"Last 7 Days","Last 30 Days", "All Time"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(timeAdapter);
        graph = (GraphView) findViewById(R.id.weekGraph);
         series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(15);
        series.setThickness(10);
        series.setColor(Color.parseColor("#bb0000"));

        graph.addSeries(series);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space
       // graph.setTitle("Last 7 Days");

// set manual x bounds to have nice steps
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = format1.format(calendar.getTime());
        Date latest = null;
        try {
             latest = format1.parse(formattedDate);
             Log.i("History",formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        formattedDate = format1.format(calendar.getTime());
        Date earliest = null;
        try {
            earliest = format1.parse(formattedDate);
            Log.i("History",formattedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        graph.getViewport().setMaxX(latest.getTime());
        graph.getViewport().setMinX(earliest.getTime());
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScrollable(true); // enables horizontal zooming and scrolling
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Steps");
        //graph.getGridLabelRenderer().setPadding(32);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Completed");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for(DataSnapshot day: snapshot.child("Users").child(currentUser.getUid()).child("History").getChildren()){
                      int numSteps = day.getValue(Integer.class);
                      String dateString = day.getKey();
                    try {
                        Date dateFromString = sdf.parse(dateString);
                        DataPoint point = new DataPoint(dateFromString,numSteps);
                        data.add(point);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                //set initial data
                DataPoint[] initData = new DataPoint[data.size()];
                for(int i = 0; i < data.size();i++){
                    initData[i] = data.get(i);
                }
                series.resetData(initData);


                User user = snapshot.child("Users").child(currentUser.getUid()).getValue(User.class);
                totalSteps.setText("Cumulative Steps Taken: " + Integer.toString(user.totalSteps()));
                recordSteps.setText("Most Steps in a Day: " + Integer.toString(user.getDailyRecord()));
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                Date accountCreationDate = null;
                try {
                    accountCreationDate = format1.parse(user.signUpDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date today = Calendar.getInstance().getTime();
                long diff = accountCreationDate.getTime() - today.getTime();
                int accountAge = (int) Math.abs(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

                if(accountAge == 0){
                    averageSteps.setText("Average Steps/Day: " + Integer.toString(user.totalSteps()));
                }
                else{
                    averageSteps.setText("Average Steps/Day: " + Integer.toString(user.totalSteps()/accountAge));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    @Override
    protected  void onResume(){
        super.onResume();
       // setUpHistory();
       // new ViewWeekStepCountTask().execute();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        for (com.google.android.gms.fitness.data.DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
              //  DataPoint currentData = new DataPoint()
                //put datapoint in dataset
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).requestEmail().build();
                GoogleSignInClient mAccount = GoogleSignIn.getClient(this,gso);
                mAccount.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                                startActivity(intent);
                            }
                        });
                break;
            case R.id.action_home:
                Intent homeIntent = new Intent(getApplicationContext(),MainAndRunningTabsScreen.class);
                startActivity(homeIntent);
                break;
            case R.id.action_FAQ:
                Intent intent = new Intent(getApplicationContext(),FaqActivity.class);
                startActivity(intent);
                break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(aboutIntent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == WEEK){
            updateGraph(WEEK);
        }
        else if(i == MONTH){
            updateGraph(MONTH);
        }
        else if(i == ALLTIME){
            updateGraph(ALLTIME);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void updateGraph(int typeCode){
        if(typeCode == WEEK){
            DataPoint[] weekData = new DataPoint[7];
            int pos = 0;
            int j = data.size() - 7;
            if(7 >= data.size()){
                j = 0;
                weekData = new DataPoint[data.size()];
            }
            for(int i = j; i < data.size();i++ ){
                weekData[pos] = data.get(i);
                pos++;
            }
            graph.setTitle("Last 7 days");
            series.resetData(weekData);
        }
        else if(typeCode == MONTH){
            DataPoint[] monthData = new DataPoint[30];
            int pos = 0;
            int j = data.size() - 30;
            if(30 >= data.size()){
                j = 0;
                monthData = new DataPoint[data.size()];
            }
            for(int i = j; i < data.size();i++ ){
                monthData[pos] = data.get(i);
                pos++;
            }
            graph.setTitle("Last 30 days");
            series.resetData(monthData);

        }
        else if(typeCode == ALLTIME){
            DataPoint [] allTimeData = new DataPoint[data.size()];
            for(int i = 0; i < data.size();i++){
                allTimeData[i] = data.get(i);
            }
            graph.setTitle("Since Sign-Up");
            series.resetData(allTimeData);

        }

    }
}
