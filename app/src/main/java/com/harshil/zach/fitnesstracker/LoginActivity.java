package com.harshil.zach.fitnesstracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button signin = (Button) findViewById(R.id.email_sign_in_button);
        Button goSignUp = (Button) findViewById(R.id.goToSignUp);
        Button goReset = (Button) findViewById(R.id.goToReset);
        firebaseAuth = FirebaseAuth.getInstance();
        goSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),SignUpActivity.class);
                startActivity(intent);
            }
        });
        signin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                authenticateUser(view.getContext());
            }
        });
        goReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createResetDialog();
            }
        });
    }
    private void authenticateUser(final Context context) {
        //get user input
        String email = mEmailView.getText().toString().trim();
       final String password = mPasswordView.getText().toString().trim();
       if(email.equals("")){
           mEmailView.setError("Can't be blank");
       }
       else if(password.equals("")){
           mPasswordView.setError("Can't be blank");
       }
       else {
           //Sign user in with email and password
           firebaseAuth.signInWithEmailAndPassword(email, password)
                   .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()) {
                               // Sign in success, update UI with the signed-in user's information
                               FirebaseUser user = firebaseAuth.getCurrentUser();
                               Intent intent = new Intent(context, MainAndRunningTabsScreen.class);
                               context.startActivity(intent);

                           } else {
                               // If sign in fails, display a message to the user.
                               Toast.makeText(LoginActivity.this, "Login failed.",
                                       Toast.LENGTH_SHORT).show();
                               // updateUI(null);
                           }

                           // ...
                       }
                   });
       }
    }
    public void createResetDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        dialogBuilder.setTitle("Reset Password");
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Send Reset Email", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText email = dialogView.findViewById(R.id.resetEmail);
                String emailAddress = email.getText().toString();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if(emailAddress.equals("")){
                    email.setError("Can't be blank");
                }
                else {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Email sent", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
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



}

