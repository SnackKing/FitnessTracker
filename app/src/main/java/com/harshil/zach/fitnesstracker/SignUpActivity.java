package com.harshil.zach.fitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.database.Cursor;

import android.os.Bundle;

import android.view.View;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //create instance of firebase
        firebaseAuth = FirebaseAuth.getInstance();
        // Set up the login form.
        mEmailView = (EditText)findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        Button goToSignIn = (Button) findViewById(R.id.goToSignIn);
        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),LoginActivity.class);
                startActivity(intent);
            }
        });

      //  mLoginFormView = findViewById(R.id.login_form);
        mEmailSignUpButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        //calling register method on click
        registerUser(view.getContext());
    }
    private void registerUser(final Context context){

        //creating a new user
        String email = mEmailView.getText().toString().trim();
        String password  = mPasswordView.getText().toString().trim();
        boolean valid = true;
        if(!isEmailValid(email) && valid){
            mEmailView.requestFocus();
            mEmailView.setError("Invalid email");
            valid = false;

        }
        //Android seem to only allow one error message at a time.
        if(!isPasswordValid(password) && valid){
            mPasswordView.requestFocus();
            mPasswordView.setError("Password must be at least 8 characters at have 1 letter, 1 number and 1 special character");
            valid = false;
        }
        if(valid) {


            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //checking if success
                    if (task.isSuccessful()) {
                        //signup successful
                        Toast.makeText(SignUpActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, StepCountActivity.class);
                        context.startActivity(intent);

                    } else {
                        //signup failed
                        Toast.makeText(SignUpActivity.this, "Error while registering", Toast.LENGTH_LONG).show();

                    }

                }
            });
        }

    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static boolean isPasswordValid(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();

    }


}

