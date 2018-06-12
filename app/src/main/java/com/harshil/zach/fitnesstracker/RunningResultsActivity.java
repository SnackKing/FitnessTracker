package com.harshil.zach.fitnesstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import models.Rank;
import models.RunningChallenge;
import models.User;

public class RunningResultsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private RunningChallenge challenge;
    private double distance;
    private String timeLeft;
    private String completed;
    private GoogleMap mMap;
    ArrayList<LatLng> polyPlaces = new ArrayList<>();
    PolylineOptions polylineOptions = new PolylineOptions();
    Polyline polyLine;
    List<String> info = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private DrawerLayout mDrawerLayout;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    int userExp = 0;
    int updatedExp = 0;
    int runRank;
    List<Rank> ranks = new ArrayList<>();
    Rank nextRank;
    boolean isFreeMode;
    boolean success = false;
    boolean keepGoing = true;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_results);

        polyPlaces = getIntent().getParcelableArrayListExtra("polyLocations");
        ListView list = (ListView) findViewById(R.id.results);
        challenge = (RunningChallenge) getIntent().getSerializableExtra("challenge");
        distance = getIntent().getDoubleExtra("distanceLeft", 0);
        timeLeft = getIntent().getStringExtra("time");
        isFreeMode = getIntent().getBooleanExtra("isFreeMode",false);
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
                            Intent intent = new Intent(RunningResultsActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(RunningResultsActivity.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(RunningResultsActivity.this, HistoryActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.history){
                            Intent intent = new Intent(RunningResultsActivity.this,HistoryActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_leaderboard){
                            Intent intent = new Intent(RunningResultsActivity.this,LeaderBoardActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String tempTimeTaken = "";
        if(!isFreeMode) {
             tempTimeTaken = getStringTimeTaken(timeLeft, challenge.getTime());
        }

        final String timeTaken = tempTimeTaken;
        if(isFreeMode) {
            success = true;
            double roundedDistance = round(distance,1);
            completed = "You ran " + roundedDistance + " miles and earned " + (int)(roundedDistance*100) + " xp";
        }
        else {
            if (distance <= 0) {
                completed = "You completed the challenge!";
                success = true;
            } else {
                completed = "You did not complete the challenge!";
            }
        }



        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                User user = dataSnapshot.child("Users").child(currentUser.getUid()).getValue(User.class);
                userExp = user.getRunXp();
                for (DataSnapshot postSnapshot : dataSnapshot.child("Ranks").getChildren()) {
                    Rank current = postSnapshot.getValue(Rank.class);
                    ranks.add(current);
                }
                runRank = user.getRunRank();
                if (success && keepGoing){
                    keepGoing = false;
                    addExperience();
                    if(!isFreeMode) {
                        String previousBest = dataSnapshot.child("RunningChallenges").child(Integer.toString(challenge.getId())).child("Leaderboard").child(currentUser.getUid()).getValue(String.class);
                        if (isBetterTime(timeTaken, previousBest)) {
                            mDatabase.child("RunningChallenges").child(Integer.toString(challenge.getId())).child("Leaderboard").child(currentUser.getUid()).setValue(timeTaken);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        info.add(completed);

        if(!isFreeMode){
            info.add("Challenge Description: " + challenge.getDescription());
            info.add("Time Remaining: " + timeLeft);
            info.add("Distance Remaining: " + Double.toString(distance) + " miles");
        }
        else{
            info.add("Time Taken: " + timeLeft);
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, info);
        list.setAdapter(adapter);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (polyPlaces.size() > 10){
            polylineOptions.addAll(polyPlaces);
            polyLine = mMap.addPolyline(polylineOptions);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int i = 0;
            while (i < polyPlaces.size()){
                builder.include(polyPlaces.get(i));
                i = i + 1;
            }
            int padding = 50;
            LatLngBounds bounds = builder.build();
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
        else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mMap.setMyLocationEnabled(true);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                break;
            case R.id.action_home:
                Intent homeIntent = new Intent(getApplicationContext(),MainAndRunningTabsScreen.class);
                startActivity(homeIntent);
                break;
            case R.id.action_FAQ:
                Intent faqIntent = new Intent(getApplicationContext(),FaqActivity.class);
                startActivity(faqIntent);
                break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(aboutIntent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void addExperience(){
            if(isFreeMode){
                updatedExp = userExp + ((int) Math.round(distance*100));
            }
            else {
                updatedExp = userExp + challenge.getXp();
            }
            mDatabase.child("Users").child(user.getUid()).child("runXp").setValue(updatedExp);
            int i = 0;
            while (i < ranks.size()){
                if (ranks.get(i).level() == runRank + 1){
                    nextRank = ranks.get(i);
                    if (updatedExp > nextRank.getXp()){
                        mDatabase.child("Users").child(user.getUid()).child("runRank").setValue(runRank+1);
                    }
                }
                i = i + 1;
            }
    }
    public static String getStringTimeTaken(String timeLeft, String timeGiven){
        int minutesLeft = Integer.parseInt(timeLeft.substring(0,timeLeft.indexOf(':')));
        int minutesGiven = Integer.parseInt(timeGiven.substring(0,timeGiven.indexOf(':')));
        int secondsLeft = Integer.parseInt(timeLeft.substring(timeLeft.indexOf(':')+1));
            minutesLeft++;

        //it is assumed that every challenge will have starting time in the form xx:00
        int millis = ((minutesGiven - minutesLeft)* 60 * 1000) + ((60 - secondsLeft)* 1000);
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }
    public static boolean isBetterTime(String timeTaken, String previousBest){
        int minutesTaken = Integer.parseInt(timeTaken.substring(0,timeTaken.indexOf(':')));
        int secondsTaken = Integer.parseInt(timeTaken.substring(timeTaken.indexOf(':')+1));
        int previousMinutesTaken = Integer.parseInt(previousBest.substring(0,previousBest.indexOf(':')));
        int previousSecondsTaken = Integer.parseInt(previousBest.substring(previousBest.indexOf(':')+1));
        int millisTaken = (minutesTaken*60*1000) + (secondsTaken*1000);
        int previousMillisTaken = (previousMinutesTaken*60*1000) + (previousSecondsTaken*1000);
        boolean isBetter = false;
        if(millisTaken < previousMillisTaken){
            isBetter = true;
        }
        return isBetter;


    }
    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
