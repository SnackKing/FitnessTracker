package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NoConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
        Button retry = findViewById(R.id.retry_connection);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cm =
                        (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(isConnected){
                    //start new activity for not having connection

                    Intent intent = new Intent(NoConnectionActivity.this,SignUpActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast toast = Toast.makeText(NoConnectionActivity.this,"No connection", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
