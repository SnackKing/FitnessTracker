package com.harshil.zach.fitnesstracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        showCompleted = true;
        ListView list = (ListView) findViewById(R.id.list);
         toggle = (Switch) findViewById(R.id.toggleChallenges);
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
            if(target.title.equals(current.title)){
                challenges.remove(i);
            }
        }
    }

}
