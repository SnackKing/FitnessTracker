package com.harshil.zach.fitnesstracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private DrawerLayout mDrawerLayout;
    FriendArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "FriendActivity";
    List<User> friends = new ArrayList<>();
    private DatabaseReference mDatabase;
    private DatabaseReference uDatabase;
    SearchView editsearch;
    User foundFriend;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        ListView list = (ListView) findViewById(R.id.friendList);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if(id == R.id.nav_account){
                            Intent intent = new Intent(FriendActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(FriendActivity.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            //Do nothing

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
        //uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Completed");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                User user = snapshot.child("Users").child(currentUser.getUid()).getValue(User.class);
                ArrayList<String> friendIds = new ArrayList<String>();
                for (DataSnapshot friendSnapShot : snapshot.child("Users").child(currentUser.getUid()).child("Friends").getChildren()) {
                    //Getting the data from snapshot
                    String id = friendSnapShot.getValue(String.class);
                    User friendFromId = snapshot.child("Users").child(id).getValue(User.class);
                    friends.add(friendFromId);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Here");
            }
        });
        adapter = new FriendArrayAdapter(this,friends);
        list.setAdapter(adapter);
        editsearch = (SearchView) findViewById(R.id.findFriend);
        editsearch.setOnQueryTextListener(this);



    }
    @Override
    public boolean onQueryTextSubmit(final String query) {

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                final String adjustedQuery = query.replace('.',',');
                final String newFriendId = dataSnapshot.child("email_uid").child(adjustedQuery).getValue(String.class);
                boolean alreadyExists = false;

                if(newFriendId == null){
                    Toast toast = Toast.makeText(FriendActivity.this,"No user found with that email",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 250);
                    toast.show();
                }

                else {
                    foundFriend = dataSnapshot.child("Users").child(newFriendId).getValue(User.class);
                    for(User current: friends){
                        if(current.getEmail().equals(foundFriend.getEmail())){
                            alreadyExists = true;
                        }
                    }
                    if(alreadyExists){
                        Toast toast = Toast.makeText(FriendActivity.this,"This user is already a friend",Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 250);
                        toast.show();

                    }
                    else {


                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FriendActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_friend, null);
                        dialogBuilder.setView(dialogView);
                        TextView name = dialogView.findViewById(R.id.name);
                        TextView email = dialogView.findViewById(R.id.email);
                        TextView rank = dialogView.findViewById(R.id.rank);
                        CircleImageView profile = dialogView.findViewById(R.id.profile_image);
                        String img = foundFriend.getProfile();
                        if(!img.equals("")){
                            byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                            profile.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                        }
                        name.setText(foundFriend.getName());
                        email.setText(foundFriend.getEmail());
                        rank.setText(Integer.toString(foundFriend.getRank()));
                        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                mDatabase.child("Users").child(user.getUid()).child("Friends").child(adjustedQuery).setValue(newFriendId);
                                friends.add(foundFriend);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });

                        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // cancel
                                dialog.dismiss();

                            }
                        });

                        dialogBuilder.show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return false;

    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
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


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
