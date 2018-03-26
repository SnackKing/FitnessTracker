package com.harshil.zach.fitnesstracker;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;

public class RunningResultsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private RunningChallenge challenge;
    private double distanceLeft;
    private String timeLeft;
    private String completed;
    private GoogleMap mMap;
    ArrayList<LatLng> polyPlaces = new ArrayList<>();
    PolylineOptions polylineOptions = new PolylineOptions();
    Polyline polyLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_results);

        polyPlaces = getIntent().getParcelableArrayListExtra("polyLocations");

        TextView timeRemaining = (TextView) findViewById(R.id.time);
        TextView distanceRemaining = (TextView) findViewById(R.id.distance);
        challenge = (RunningChallenge) getIntent().getSerializableExtra("challenge");
        distanceLeft = getIntent().getDoubleExtra("distanceLeft", 0);
        timeLeft = getIntent().getStringExtra("time");
        timeRemaining.setText(timeLeft);
        distanceRemaining.setText(Double.toString(distanceLeft));

        if (distanceLeft <= 0){
            completed = "You completed the challenge!";
        }
        else{
            completed = "You did not complete the challenge!";
        }
        TextView resultText = (TextView) findViewById(R.id.completion);
        resultText.setText(completed);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);





    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
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
}
