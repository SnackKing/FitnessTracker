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
   // ArrayAdapter<String> adapter;
    List<Challenge> challenges = new ArrayList<>();
    private static final String TAG = "ChallengeActivity";
    CustomArrayAdapter adapter;
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
        System.out.println(mDatabase.child("Challenges"));


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
