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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
   // ArrayAdapter<String> adapter;
   ArrayAdapter<String> arrayAdapter;
    List<Challenge> challenges = new ArrayList<>();
    private static final String TAG = "ChallengeActivity";
    boolean includeCompletedChallenges;
    Switch toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        Log.i(TAG, "Created challenges view");
        includeCompletedChallenges = true;
        ListView list = (ListView) findViewById(R.id.list);
         toggle = (Switch) findViewById(R.id.toggleChallenges);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Challenge1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        System.out.println(mDatabase.child("Challenges"));
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Getting the data from snapshot
                    Challenge challenge = postSnapshot.getValue(Challenge.class);
                    boolean isCompleted = challenge.isCompleted();
                    if(includeCompletedChallenges) {
                        if (isCompleted) {
                            challenges.add(challenge);
                        }
                    }
                    else{
                        if(!isCompleted){
                            challenges.add(challenge);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Here");
            }
        });

        Challenge challenge = new Challenge(false,1,500,"test",100);
        challenges.add(challenge);
        System.out.println(challenges.size());
        list.setAdapter(new CustomArrayAdapter(this, challenges));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(toggle.isChecked()){
                    includeCompletedChallenges = false;
                    //re-pull data from firebase
                }
                else{
                    includeCompletedChallenges = true;
                    //re-pull data from firebase
                }
            }
        });

    }
}
