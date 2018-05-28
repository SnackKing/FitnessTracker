package com.harshil.zach.fitnesstracker;

import android.app.Notification;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private DatabaseReference mDatabase;
    ListView list;
    Map<String,String> notifications = new HashMap<>();

    private static final String TAG = "ChallengeActivity";
    NotificationAdapter adapter;
    FirebaseUser user;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_notifications);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        user = FirebaseAuth.getInstance().getCurrentUser();
        list = findViewById(R.id.list);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id =menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(NotificationsActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(NotificationsActivity.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(NotificationsActivity.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.history){
                            Intent intent = new Intent(NotificationsActivity.this,HistoryActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_leaderboard){
                            Intent intent = new Intent(NotificationsActivity.this,LeaderBoardActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot currentNotification: dataSnapshot.child("Users").child(user.getUid()).child("Updates").child("All").getChildren()){
                    String shortenedDate = shortenDate(currentNotification.getKey());
                    notifications.put(shortenedDate,currentNotification.getValue(String.class));
                }
                adapter = new NotificationAdapter(notifications);
                list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"An error occurred",Toast.LENGTH_SHORT).show();
            }
        });




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
    public String shortenDate(String longDate){
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dateValue = null;
        try {
            dateValue = input.parse(longDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy");
        if(dateValue != null) {
            return output.format(dateValue);
        }
        else{
            return "";
        }
    }


}
