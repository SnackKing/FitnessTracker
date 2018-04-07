package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Firebase.setAndroidContext(this);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            //start new activity for not having connection
            Intent intent = new Intent(SplashActivity.this,NoConnectionActivity.class);
            startActivity(intent);
        }
        else {


            if (user != null) {
                Intent intent = new Intent(getApplicationContext(), MainAndRunningTabsScreen.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
