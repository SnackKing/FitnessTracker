package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RunningPage extends Fragment {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    int userExp;
    int rank;
    DonutProgress progress;
    List<String> challengeDescriptions = new ArrayList<>();
    ArrayAdapter<String> adapter;
    List<RunningChallenge> challenges = new ArrayList<>();
    List<Rank> ranks = new ArrayList<>();
    Rank nextRank;
    int runRank;
    int percentage = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_running_page, container, false);



        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ListView list = view.findViewById(R.id.challengeList);
        final TextView rankDescription = (TextView) view.findViewById(R.id.rank);
        progress = view.findViewById(R.id.donut_progress);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                User user = dataSnapshot.child("Users").child(currentUser.getUid()).getValue(User.class);
                userExp = user.getRunXp();

                runRank = user.getRunRank();
                ranks.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.child("Ranks").getChildren()) {
                    Rank current = postSnapshot.getValue(Rank.class);
                    ranks.add(current);
                }
                setView();
                rankDescription.setText(Integer.toString(runRank));
                challengeDescriptions.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.child("RunningChallenges").getChildren()) {
                    //Getting the data from snapshot
                    RunningChallenge challenge = postSnapshot.getValue(RunningChallenge.class);
                    challenges.add(challenge);
                    challengeDescriptions.add(challenge.getDescription());
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Something bad happened
            }


        });
        adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, challengeDescriptions);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(getActivity(), RunningChallengePage.class);
                intent.putExtra("challenge", challenges.get(position));
                startActivity(intent);
            }
        });

        return view;
    }

    private void setView(){

        float decimal = 0;
        float percent = 0;
        int i = 0;
        while (i < ranks.size()){
            if (ranks.get(i).level() == runRank + 1){
                nextRank = ranks.get(i);
                decimal = (float)userExp/nextRank.getXp();
                percent = decimal * 100;
            }
            i = i + 1;
        }

        progress.setDonut_progress(Integer.toString(Math.round(percent)));
        progress.setText(Integer.toString(userExp));


    }
}
