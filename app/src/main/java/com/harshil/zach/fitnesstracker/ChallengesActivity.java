package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import adapters.CustomArrayAdapter;
import models.Challenge;
import models.FriendChallenge;

public class ChallengesActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference uDatabase;
    // ArrayAdapter<String> adapter;
    List<Challenge> challenges = new ArrayList<>();

    private static final String TAG = "ChallengeActivity";
    CustomArrayAdapter adapter;
    boolean showCompleted;
    Switch toggle;
    private FirebaseAuth firebaseAuth;
    private DrawerLayout mDrawerLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        showCompleted = true;
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        ListView list = (ListView) findViewById(R.id.list);
         toggle = (Switch) findViewById(R.id.toggleChallenges);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(ChallengesActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
//                            Intent intent = new Intent(ChallengesActivity.this,ChallengesActivity.class);
//                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(ChallengesActivity.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.history){
                            Intent intent = new Intent(ChallengesActivity.this,HistoryActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_leaderboard){
                            Intent intent = new Intent(ChallengesActivity.this,LeaderBoardActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Completed");




        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.child("Challenges").getChildren()) {
                    //Getting the data from snapshot
                    Challenge challenge = postSnapshot.getValue(Challenge.class);
                    challenges.add(challenge);

                }
                for(DataSnapshot postSnapshot : snapshot.child("FriendChallenges").child("AddFriends").getChildren()){
                    FriendChallenge friendChallenge = postSnapshot.getValue(FriendChallenge.class);
                    Challenge friendToChallenge = new Challenge(0,friendChallenge.getNumFriends(),friendChallenge.getTitle(),friendChallenge.getXp(),friendChallenge.getId(),"");
                    challenges.add(friendToChallenge);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Here");
            }
        });


        adapter = new CustomArrayAdapter(this,challenges);
        list.setAdapter(adapter);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(toggle.isChecked()){
                    showCompleted = false;
                    //re-pull data from firebase
                    pullData();
                }
                else{
                    showCompleted = true;
                    //re-pull data from firebase
                    pullData();
                }
            }
        });

    }
    public void pullData(){
        uDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!showCompleted) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Challenge challenge = postSnapshot.getValue(Challenge.class);
                        removeChallenge(challenge);
                    }
                    adapter.notifyDataSetChanged();
                }
                else{
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Challenge challenge = postSnapshot.getValue(Challenge.class);
                        challenges.add(challenge);
                    }
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
    }
    public void removeChallenge(Challenge target){
        for(int i = 0; i < challenges.size();i++){
            Challenge current = challenges.get(i);
            int x;
            if(target.title.equals(current.title)){
                challenges.remove(i);
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

}
