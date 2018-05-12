package com.harshil.zach.fitnesstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    final String TAG = "Profile";


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
        ImageButton updateEmail = findViewById(R.id.updateEmail);
        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_update_email, null);
                final AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setView(dialogView)
                        .setTitle("Change Email")
                        .setPositiveButton("OK", null) //Set to null. We override the onclick
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {


                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                //validate email
                                final EditText newEmail = dialog.findViewById(R.id.newEmail);
                                final String newEmailString = newEmail.getText().toString();
                                final EditText password = dialog.findViewById(R.id.password);
                                final String passwordString = password.getText().toString();
                                if (isEmailValid(newEmailString) && !newEmailString.equals("") && !passwordString.equals("")) {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String oldEmail = user.getEmail();
                                    final String altOldEmail = oldEmail.replace('.', ',');


                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(user.getEmail(), passwordString);
                                    // Prompt the user to re-provide their sign-in credentials
                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener < Void > () {
                                                @Override
                                                public void onComplete(@NonNull Task < Void > task) {
                                                    if(task.isSuccessful()) {


                                                        Log.d("ProfileActivity", "User re-authenticated.");
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        user.updateEmail(newEmailString)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d("ProfileActivity", "User email address updated.");
                                                                            email.setText(newEmailString);
                                                                            mDatabase.child("email").setValue(newEmailString);
                                                                            final String formattedEmail = newEmailString.replace('.', ',');
                                                                            //UPDATE EMAIL_UID TABLE******
                                                                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                            //remove old email from email_uid table
                                                                            FirebaseDatabase.getInstance().getReference().child("email_uid").child(altOldEmail).setValue(null);
                                                                            FirebaseDatabase.getInstance().getReference().child("email_uid").child(formattedEmail).setValue(user.getUid());
                                                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                                                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    //this is pretty gross, but we need to update all of the current user's friend's references to reflect the new email
                                                                                    for (DataSnapshot currentFriend : dataSnapshot.child("FriendedBy").getChildren()) {
                                                                                        String email = currentFriend.getKey();
                                                                                        String friendId = currentFriend.getValue(String.class);
                                                                                        String altEmail = email.replace(',', '.');
                                                                                        String userAltEmail = user.getEmail().replace('.', ',');
                                                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(friendId).child("Friends").child(altOldEmail).setValue(null);
                                                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(friendId).child("Friends").child(formattedEmail).setValue(user.getUid());
                                                                                    }
                                                                                    //update friendedBy references for all users
                                                                                    for (DataSnapshot currentFriend : dataSnapshot.child("Friends").getChildren()) {
                                                                                        String email = currentFriend.getKey();
                                                                                        String friendId = currentFriend.getValue(String.class);
                                                                                        String altEmail = email.replace(',', '.');
                                                                                        String userAltEmail = user.getEmail().replace('.', ',');
                                                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(friendId).child("FriendedBy").child(altOldEmail).setValue(null);
                                                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(friendId).child("FriendedBy").child(formattedEmail).setValue(user.getUid());

                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {
                                                                                    Log.d("UPDATEEMAIL", databaseError.getMessage());

                                                                                }
                                                                            });
                                                                            dialog.dismiss();
                                                                        } else {
                                                                            newEmail.setError("Email already taken");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                    else{
                                                        password.setError("Incorrect Password");
                                                    }
                                                }
                                            });
                                } else {
                                    if(passwordString.equals("")){
                                        password.setError("Can't be blank");
                                    }
                                    else{
                                        newEmail.setError("Invalid email");
                                    }

                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
        ImageButton updateName = findViewById(R.id.updateName);
        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_update_name, null);
                dialogBuilder.setTitle("Enter New Name");
                dialogBuilder.setView(dialogView);
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        EditText newName = dialogView.findViewById(R.id.newName);
                        String newNameString = newName.getText().toString();
                        if (!newNameString.equals("")) {
                            mDatabase.child("name").setValue(newNameString);
                            name.setText(newNameString);
                        }

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
        });


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();
                        if (id == R.id.nav_account) {
                            //                            Intent intent = new Intent(ProfileActivity.this,ProfileActivity.class);
                            //                            startActivity(intent);
                        } else if (id == R.id.nav_challenges) {
                            Intent intent = new Intent(ProfileActivity.this, ChallengesActivity.class);
                            startActivity(intent);
                        } else if (id == R.id.nav_friends) {
                            Intent intent = new Intent(ProfileActivity.this, FriendActivity.class);
                            startActivity(intent);
                        } else if (id == R.id.history) {
                            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
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
                DataSnapshot friends = snapshot.child("Friends");
                int friendCount = 0;
                for (DataSnapshot current: friends.getChildren()) {
                    friendCount++;
                }
                numFriends.setText(Integer.toString(friendCount));
                System.out.println("PRINTING URL");
                String img = user.getProfile();
                if (!img.equals("") && img != null) {
                    byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
                    profilePic.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Here");
            }
        });

        Button changePassword = findViewById(R.id.changePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_update_password, null);
                final AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setView(dialogView)
                        .setTitle("Change Password")
                        .setPositiveButton("OK", null) //Set to null. We override the onclick
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                final EditText oldPass = dialogView.findViewById(R.id.oldPass);
                                EditText newPass = dialogView.findViewById(R.id.newPass);
                                EditText confirmNewPass = dialogView.findViewById(R.id.confirmPass);
                                String oldPassString = oldPass.getText().toString();
                                final String newPassString = newPass.getText().toString();
                                String confirmPassString = confirmNewPass.getText().toString();
                                boolean valid = true;
                                if (!newPassString.equals(confirmPassString)) {
                                    confirmNewPass.setError("Does not match");
                                    valid = false;
                                }
                                if (!SignUpActivity.isPasswordValid(newPassString)) {
                                    newPass.setError("Password must be at least 8 characters at have 1 letter, 1 number and 1 special character");
                                    valid = false;
                                }
                                if(oldPassString.equals("")){
                                    valid = false;
                                    oldPass.setError("Can't be blank");
                                }
                                if (valid) {
                                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String userEmail = user.getEmail();
                                    AuthCredential credential = EmailAuthProvider.getCredential(userEmail, oldPassString);
                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener < Void > () {
                                                @Override
                                                public void onComplete(@NonNull Task < Void > task) {
                                                    if (task.isSuccessful()) {
                                                        user.updatePassword(newPassString).addOnCompleteListener(new OnCompleteListener < Void > () {
                                                            @Override
                                                            public void onComplete(@NonNull Task < Void > task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Password updated");
                                                                    Toast.makeText(getApplicationContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();

                                                                } else {
                                                                    Log.d(TAG, "Error password not updated");
                                                                    Toast.makeText(getApplicationContext(), "A Problem Occurred", Toast.LENGTH_SHORT).show();
                                                                    dialog.dismiss();

                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Log.d(TAG, "Error auth failed");
                                                        oldPass.setError("Incorrect Password");
                                                    }
                                                }
                                            });


                                }

                            }
                        });

                    }
                });
                dialog.show();
                Button negative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

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
        GoogleSignInClient mAccount = GoogleSignIn.getClient(this, gso);
        mAccount.signOut()
        .addOnCompleteListener(this, new OnCompleteListener < Void > () {
@Override
public void onComplete(@NonNull Task < Void > task) {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        }
        });
        break;
        case R.id.action_home:
        Intent homeIntent = new Intent(getApplicationContext(), MainAndRunningTabsScreen.class);
        startActivity(homeIntent);
        break;
        case R.id.action_FAQ:
        Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
        startActivity(intent);
            break;
            case R.id.action_about:
                Intent aboutIntent = new Intent(getApplicationContext(),AboutActivity.class);
                startActivity(aboutIntent);
                break;



        }
        return super.onOptionsItemSelected(item);
        }


@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
        if (data != null) {
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
public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
        }

        }