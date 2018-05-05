package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LeaderBoardActivity  extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    DatabaseReference mDatabase;
    FirebaseUser user;
    private Map<String, String> leaderBoardForCurrentChallenge = new HashMap<>();
    private ArrayList<String> friends = new ArrayList<>();
    private  ArrayList<RunningChallenge> challenges = new ArrayList<>();
    int currentChallengeId = 0;
    Spinner spinner;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_leader_board);
        listView = findViewById(R.id.list);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.leaderboard_header,listView,false);
        listView.addHeaderView(header);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
       mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(LeaderBoardActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
//                            Intent intent = new Intent(ChallengesActivity.this,ChallengesActivity.class);
//                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(LeaderBoardActivity.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.history){
                            Intent intent = new Intent(LeaderBoardActivity.this,HistoryActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_leaderboard){
                            Intent intent = new Intent(LeaderBoardActivity.this,LeaderBoardActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });
        spinner = (Spinner) findViewById(R.id.challengeType);
        spinner.setOnItemSelectedListener(this);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int numChallenges = 0;
                for(DataSnapshot challenge: dataSnapshot.child("RunningChallenges").getChildren()){
                    challenges.add(challenge.getValue(RunningChallenge.class));
                    numChallenges++;
                }
                for(DataSnapshot friend: dataSnapshot.child("Users").child(user.getUid()).child("Friends").getChildren()){
                    friends.add(friend.getValue(String.class));
                }
                String[] spinnerArray = new String[numChallenges];
                for(int i = 0; i < challenges.size();i++){
                    spinnerArray[i] = Double.toString(challenges.get(i).getDistance()) + " Mile(s) in " + challenges.get(i).getTime();
                }
                ArrayAdapter<String> challengeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);
                challengeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(challengeAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        //pos corresponds to id of challenge
        getLeaderboard(i+1);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void getLeaderboard(final int challengeId){
        leaderBoardForCurrentChallenge.clear();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot current: dataSnapshot.child("RunningChallenges").child(Integer.toString(challengeId)).child("Leaderboard").getChildren()){
                        String username = dataSnapshot.child("Users").child(current.getKey()).child("name").getValue(String.class);
                        leaderBoardForCurrentChallenge.put(username,current.getValue(String.class));
                    }
                    leaderBoardForCurrentChallenge = sortByComparator(leaderBoardForCurrentChallenge, true);
                    LeaderboardAdapter adapter = new LeaderboardAdapter(leaderBoardForCurrentChallenge);
                    listView.setAdapter(adapter);

                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    //https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    private static Map<String, String> sortByComparator(Map<String, String> unsortMap, final boolean order)
    {

        List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String, String>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, String>>()
        {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }


}


