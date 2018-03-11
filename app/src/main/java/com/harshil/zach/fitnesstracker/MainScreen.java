package com.harshil.zach.fitnesstracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import static java.lang.Math.toIntExact;


public class MainScreen extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DrawerLayout mDrawerLayout;
    private DatabaseReference mDatabase;

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    GoogleApiClient mClient;
    OnDataPointListener listener;
    TextView stepCount;
    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    private static final String TAG = "MainScreenActivity";
    int userExp;
    long lastCheckedSteps;
    int userRank;
    DonutProgress progress;
    TextView rank;
    ArrayList<Rank> ranks;
    Rank userNextRank;
    FirebaseUser user;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ranks = new ArrayList<>();
        userExp = -1;
        userRank = -1;
        lastCheckedSteps = 0;


        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        firebaseAuth = FirebaseAuth.getInstance();
       user = firebaseAuth.getCurrentUser();
         progress = findViewById(R.id.donut_progress);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rankDatabase = FirebaseDatabase.getInstance().getReference().child("Ranks");
        rank = findViewById(R.id.rank);
        stepCount = (TextView) findViewById(R.id.stepDisplay);

        Button allChallenges = (Button) findViewById(R.id.challenges);
        allChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toChallengesPage = new Intent(MainScreen.this, ChallengesActivity.class);
                startActivity(toChallengesPage);
            }
        });
        Button friends = (Button) findViewById(R.id.friendsButton);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToFriendsPage = new Intent(MainScreen.this, ProfileActivity.class);
                startActivity(goToFriendsPage);
            }
        });
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            //  accessGoogleFit();
            subscribe();
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id =menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(MainScreen.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(MainScreen.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(MainScreen.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                User user = dataSnapshot.child("Users").child(currentUser.getUid()).getValue(User.class);
                userExp = user.xp();
                userRank = user.getRank();
                rank.setText(Integer.toString(userRank));

                for (DataSnapshot postSnapshot : dataSnapshot.child("Ranks").getChildren()) {
                    Rank current = postSnapshot.getValue(Rank.class);
                    ranks.add(current);
                }
                if(ranks.size() != 0){
                    userNextRank = ranks.get(userRank);
                }
                readData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                    //Something bad happened
            }
        });
        //get list of ranks
//        rankDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Rank current = postSnapshot.getValue(Rank.class);
//                    ranks.add(current);
//                }
//                if(userRank != -1){
//                    userNextRank = ranks.get(userRank);
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                //something bad happened
//            }
//        });


    }
    @Override
    protected void onResume(){
        super.onResume();
      //  readData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                subscribe();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
                                stepCount.setText(Long.toString(total) + " steps");
                                Toast.makeText(MainScreen.this,"Updating step count",Toast.LENGTH_SHORT).show();
                                addExperience(total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                                Toast.makeText(MainScreen.this,"A problem occurred",Toast.LENGTH_SHORT).show();

                            }
                        });
    }
    private void addExperience(final long total){
        SharedPreferences sharedPref= getSharedPreferences("mypref", 0);
        lastCheckedSteps = sharedPref.getLong("lastChecked", 0);
        int exp = (int) total - (int)lastCheckedSteps;
        exp = exp/100;
        mDatabase.child("Users").child(user.getUid()).child("xp").setValue(userExp + exp);
        userExp += exp;
        calculatePercent();
        SharedPreferences.Editor editor= sharedPref.edit();
        editor.putLong("lastChecked", total);
        editor.apply();
    }
    private void calculatePercent(){
        float decimal = (float)userExp/userNextRank.getXp();
        float percent = decimal * 100;
        progress.setDonut_progress(Integer.toString(Math.round(percent)));
        progress.setText(userExp + "/" + userNextRank.getXp());
    }




}
