package com.harshil.zach.fitnesstracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private DatabaseReference mDatabase;
    TextView name;
    TextView email;
    TextView rank;
    TextView runRank;
    TextView numFriends;
    private FirebaseAuth firebaseAuth;
    public static final int PICK_IMAGE = 1;
    CircleImageView profilePic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        firebaseAuth = FirebaseAuth.getInstance();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        profilePic = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        rank = findViewById(R.id.rank);
        runRank = findViewById(R.id.runRank);
        ImageButton updateProfile = findViewById(R.id.updatePhoto);
        numFriends = findViewById(R.id.numFriends);
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE);
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if(id == R.id.nav_account){
//                            Intent intent = new Intent(ProfileActivity.this,ProfileActivity.class);
//                            startActivity(intent);
                        }
                        else if(id == R.id.nav_challenges){
                            Intent intent = new Intent(ProfileActivity.this,ChallengesActivity.class);
                            startActivity(intent);
                        }
                        else if(id == R.id.nav_friends){
                            Intent intent = new Intent(ProfileActivity.this,FriendActivity.class);
                            startActivity(intent);
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return true;
                    }
                });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot);
                User user = snapshot.getValue(User.class);
                name.setText(user.getName());
                email.setText(user.email);
                rank.setText(Integer.toString(user.getRank()));
                runRank.setText(Integer.toString(user.getRunRank()));
                DataSnapshot friends = snapshot.child("friends");
                int friendCount = 0;
                for(DataSnapshot current: friends.getChildren()){
                    friendCount++;
                }
                numFriends.setText(Integer.toString(friendCount));
                System.out.println("PRINTING URL");
                String img = user.getProfile();
                if(!img.equals("") && img != null) {
                    byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                    profilePic.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Here");
            }
        });
            //profile photo null?




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
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                break;
            case R.id.action_home:
                Intent homeIntent = new Intent(getApplicationContext(),MainScreen.class);
                startActivity(homeIntent);
                break;
            case R.id.action_Faq:
                //launch faq activity
            case R.id.action_about:
                //launch about activity
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if(data != null) {
                Uri selectedImage = data.getData();
                profilePic.setImageURI(selectedImage);
                Bitmap bm = null;
                try {
                    bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                FirebaseUser user = firebaseAuth.getCurrentUser();
                //store result in firebase
                mDatabase.child("profile").setValue(encodedImage);
            }

        }
    }

}
