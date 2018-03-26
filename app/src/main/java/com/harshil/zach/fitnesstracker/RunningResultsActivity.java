package com.harshil.zach.fitnesstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
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
import android.widget.TextView;

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

public class RunningResultsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private RunningChallenge challenge;
    private double distanceLeft;
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
    boolean success = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_results);

        polyPlaces = getIntent().getParcelableArrayListExtra("polyLocations");
        ListView list = (ListView) findViewById(R.id.results);
        challenge = (RunningChallenge) getIntent().getSerializableExtra("challenge");
        distanceLeft = getIntent().getDoubleExtra("distanceLeft", 0);
        timeLeft = getIntent().getStringExtra("time");

        if (distanceLeft <= 0){
            completed = "You completed the challenge!";
            success = true;

        }
        else{
            completed = "You did not complete the challenge!";
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                if (success){
                    addExperience();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });




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
                            //Do nothing

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

        info.add(completed);
        info.add("Challenge Description: " + challenge.getDescription());
        info.add("Time Remaining: " + timeLeft);
        info.add("Distance Remaining: " + Double.toString(distanceLeft) + " miles");
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
        else{
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate center= CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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

        }
        return super.onOptionsItemSelected(item);
    }

    private void addExperience(){

            updatedExp = userExp + challenge.getXp();
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


}