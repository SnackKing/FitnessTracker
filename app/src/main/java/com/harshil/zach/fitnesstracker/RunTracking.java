package com.harshil.zach.fitnesstracker;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class RunTracking extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener  {


    private GoogleMap mMap;
    List<String> info = new ArrayList<>();
    ArrayAdapter<String> adapter;
    FloatingActionButton stop;
    RunningChallenge challenge;
    String time;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "TAG";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    Location originalLocation;
    double distance;
    float distanceTraveled = 0;
    double distanceInMiles;
    PolylineOptions polyLineOptions = new PolylineOptions();
    Polyline polyLine;
    double distanceRemaining;
    String timeRemaining;
    ArrayList<LatLng> polyLocations = new ArrayList<>();
    CountDownTimer runTimer;
    CountDownTimer startTimer;
    LocationCallback locationCallback;
    LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_tracking);

        Intent intent = getIntent();
        challenge = (RunningChallenge) intent.getSerializableExtra("challenge");
        timeRemaining = challenge.getTime();
        ListView list = findViewById(R.id.challengeDescription);
        distance = challenge.getDistance();
        distanceRemaining = distance;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);

        stop = (FloatingActionButton) findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRunTracking();
            }
        });

        info.clear();
        String description = challenge.getDescription();
        String distance = Double.toString(challenge.getDistance());
        time = challenge.getTime();
        String xp = Integer.toString(challenge.getXp());
        info.add("Distance: " + distance + " miles");
        info.add("Time: " + time);
        info.add("Start in: ");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, info);
        list.setAdapter(adapter);


        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());


    }



    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }
        }
        startTimer = new CountDownTimer(10000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                info.set(info.size()-1, "Starts in: " + Long.toString(millisUntilFinished/1000));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
                info.set(info.size()-1, "START!");
                adapter.notifyDataSetChanged();
                startRun();

            }
        }.start();
    }


    @SuppressLint("MissingPermission")
    private void startRun(){

        originalLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        info.set(0, "Distance remaining: " + Double.toString(distance));
        adapter.notifyDataSetChanged();
        int mins = challenge.getTimeMinutes(time);
        int secs = challenge.getTimeSeconds(time);
        long minToMilli = mins * 60000;
        long secToMilli = secs * 1000;


        runTimer = new CountDownTimer(minToMilli + secToMilli, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                long min = (millisUntilFinished/1000)/60;
                long sec = (millisUntilFinished/1000)%60;
                if (sec < 10){
                    timeRemaining = Long.toString(min) + ":0" + Long.toString(sec);
                    info.set(1, "Time remaining: " + timeRemaining);
                }
                else {
                    timeRemaining = Long.toString(min) + ":" + Long.toString(sec);
                    info.set(1, "Time remaining: " + timeRemaining);
                }
                adapter.notifyDataSetChanged();
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(RunTracking.this, "GPS Connection lost!", Toast.LENGTH_LONG).show();
                    endRunTracking();
                }
            }

            @Override
            public void onFinish() {
                timeRemaining = "0:00";
                endRunTracking();
            }
        }.start();


    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        };
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

       if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
           if (location != null) {
               double currentLatitude = location.getLatitude();
               double currentLongitude = location.getLongitude();
               LatLng latLng = new LatLng(currentLatitude, currentLongitude);
               CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
               CameraUpdate zoom = CameraUpdateFactory.zoomTo(17);
               mMap.moveCamera(center);
               mMap.animateCamera(zoom);
               polyLineOptions.add(latLng);
               polyLine = mMap.addPolyline(polyLineOptions);
               polyLocations.add(latLng);

               if (originalLocation != null) {
                   distanceTraveled = distanceTraveled + location.distanceTo(originalLocation);
                   originalLocation = location;
                   distanceInMiles = distanceTraveled * 0.00062137;
                   distanceRemaining = distance - distanceInMiles;
                   distanceRemaining = Math.round(distanceRemaining * 100.0) / 100.0;
                   if (distanceRemaining <= 0) {
                       distanceRemaining = 0.00;
                       endRunTracking();
                   }
                   info.set(0, "Distance Remaining: " + Double.toString(distanceRemaining));
                   adapter.notifyDataSetChanged();

               }
           }
       }
       else{
           Toast.makeText(this, "GPS Connection Lost!", Toast.LENGTH_LONG).show();
           endRunTracking();
       }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    private void stopLocationUpdates() {
        getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

    }

    private void endRunTracking(){
        stopLocationUpdates();
        if (runTimer != null){
            runTimer.cancel();
        }
        if (startTimer != null){
            startTimer.cancel();
        }
        Intent intent = new Intent(RunTracking.this, RunningResultsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("time", timeRemaining);
        intent.putExtra("distanceLeft", distanceRemaining);
        intent.putExtra("challenge", challenge);
        Bundle b = new Bundle();
        b.putParcelableArrayList("polyLocations", polyLocations);
        intent.putExtras(b);
        startActivity(intent);
        RunTracking.this.finish();
    }
}
