package com.example.keima.ambulife;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.view.View.GONE;

public class SigninScreen extends AppCompatActivity {

    // Declare Firebase object
    private FirebaseAuth mAuth;
//    private FirebaseUser currentUser;

    // Declare Views here
    TextView signinText, signinText2, signinTitleText;
    EditText signin_username, signin_password;
    Button gotoRegister, btnSignin;
    ProgressBar progressBar;



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_screen);

        // Instantiate Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Assign views by id
        signinText = (TextView) findViewById(R.id.signinText);
        signinText2 = (TextView) findViewById(R.id.signinText2);
        signinTitleText = (TextView) findViewById(R.id.signinLogoText);
        signin_username = (EditText) findViewById(R.id.signinUsername);
        signin_password = (EditText) findViewById(R.id.signinPassword);
        btnSignin = (Button) findViewById(R.id.btnSignin);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        SharedPreferences shared = getSharedPreferences("userInfo", MODE_PRIVATE);
        shared.edit().clear();
        shared.edit().commit();



        gotoRegister = (Button) findViewById(R.id.btnBackToRegister);

        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninScreen.this, RegisterScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

    }

    // Check if there is a user currently signed in
    // If there is a current user signed in, then the user must log out first
    public void onStart() {
        super.onStart();
        // Check if the user is currently signed in or not null. Then update UI
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            userExists(user);
        }
    }


    // If user signed in, change UI
    public void userExists(FirebaseUser user){
        signin_username.setVisibility(GONE);
        signin_password.setVisibility(GONE);

        signinText.setText("Welcome, "+user.getEmail());
        signinText2.setVisibility(View.VISIBLE);
        gotoRegister.setText("Sign out");

        if(isNetworkAvailable()){
        final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        final StorageReference file_storage = FirebaseStorage.getInstance().getReference("SelfiesWithValidID");
        final String storage_file_name = "Selfie_Validation_"+current_user.getUid();

            // This code block checks if there is a validation folder under the current user
            // If there's no folder found, create a new folder for the validation picture
            file_storage.child(current_user.getUid()).child(storage_file_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Folder exists
                    // btnSignin will now automatically sign in user
                    btnSignin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!isNetworkAvailable()){
                                Toast.makeText(SigninScreen.this, "You are offline. Please check internet connection", Toast.LENGTH_LONG).show();
                            }

                            DatabaseReference status = FirebaseDatabase.getInstance().getReference("profiles")
                                    .child(mAuth.getCurrentUser().getUid()).child("status");
                            status.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String type = dataSnapshot.getValue(String.class);

                                    if ("Pending".equals(type)){
                                        Toast.makeText(SigninScreen.this, "Wait for the confirmation",Toast.LENGTH_LONG).show();
                                        signOutUser();
                                    }
                                    else{
                                        Intent intent = new Intent(SigninScreen.this, TrackerActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                    // gotoRegister will now become sign out button
                    gotoRegister.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!isNetworkAvailable()){
                                Toast.makeText(SigninScreen.this, "You are offline. Please check internet connection.", Toast.LENGTH_LONG).show();
                            }else {
                                signOutUser();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Folder not found
                    // btnSignin will now automatically sign in user
                    btnSignin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!isNetworkAvailable()){
                                Toast.makeText(SigninScreen.this, "You are offline. Please check internet connection.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SigninScreen.this, TrackerActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(SigninScreen.this, SelfieValidation.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                    if(isNetworkAvailable()){
                        // gotoRegister will now become sign out button
                        gotoRegister.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(isNetworkAvailable())
                                    {signOutUser();}
                                else
                                {
                                    Toast.makeText(SigninScreen.this, "You are offline. Check your internet connection", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });


            // gotoRegister will now become sign out button
            gotoRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signOutUser();
                }
            });
        }else {
            // btnSignin will now automatically sign in user
            btnSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isNetworkAvailable()){
                        Toast.makeText(SigninScreen.this, "You are offline. Please check internet connection", Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent(SigninScreen.this, TrackerActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        // gotoRegister will now become sign out button
        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()){
                    signOutUser();
                }else {
                    Toast.makeText(SigninScreen.this, "You are offline. Please check your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    // Sign out user
    public void signOutUser(){

        // Hide Buttons
        progressBar.setVisibility(View.VISIBLE);
        toggleSignInFields(View.GONE);
        // Sign the user out
        mAuth.signOut();

        stopService(new Intent(this, TrackerService.class));
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms
                refreshActivity();
                Toast.makeText(getApplicationContext(), "You've signed out",
                        Toast.LENGTH_LONG).show();
            }
        }, 2000);
    }


    // Refresh the current activity
    public void refreshActivity(){
        finish();
        startActivity(getIntent());
    }


    // Function to toggle registration form enable(VISIBLE)/disable(GONE)
    public void toggleSignInFields(Integer visibility){
        signin_username.setVisibility(visibility);
        signin_password.setVisibility(visibility);
        btnSignin.setVisibility(visibility);
        gotoRegister.setVisibility(visibility);
    }

    public void signInUser(){
        String email = signin_username.getText().toString().trim();
        String password = signin_password.getText().toString().trim();

        // Check  the fields if filled in correctly
        if (email.isEmpty()) {
            signin_username.setError("Please enter your username");
            signin_username.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            signin_username.setError("Please enter a valid email");
            signin_username.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            signin_password.setError("Please enter your password");
            signin_password.requestFocus();
            return;
        }

        // Show Progress bar disable fields
        toggleSignInFields(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Sign in user
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    progressBar.setVisibility(View.GONE);
                    toggleSignInFields(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Signed in successfully", Toast.LENGTH_LONG).show();
                    refreshActivity();

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    toggleSignInFields(View.VISIBLE);
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
