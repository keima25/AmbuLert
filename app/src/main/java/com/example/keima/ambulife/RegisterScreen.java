package com.example.keima.ambulife;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterScreen extends AppCompatActivity{

    EditText    editText_regFirstname, editText_regLastname,
                editText_regUsername, editText_regPassword,
                editText_regCPassword, editText_Phone;
    RadioButton radioBtnTerms;
    Button      btnRegister, btnGotoSignin;
    ProgressBar progressBar;
    RadioButton rb;
    RadioGroup rg;

    // Declare Firebase object
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Declare Firebase Database Reference
    DatabaseReference databaseRegisterUser;

    public void onStart() {
        super.onStart();
        // Check if the user is currently signed in or not null. Then update UI
        FirebaseUser tmp = mAuth.getCurrentUser();
        currentUser = tmp;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        // Instantiate Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        databaseRegisterUser = FirebaseDatabase.getInstance().getReference("profiles");

        // Initialize views by id
        editText_regFirstname = (EditText) findViewById(R.id.registerFirstName);
        editText_regLastname = (EditText) findViewById(R.id.registerLastName);
        editText_regUsername = (EditText) findViewById(R.id.registerUsername);
        editText_regPassword = (EditText) findViewById(R.id.registerPassword);
        editText_regCPassword = (EditText) findViewById(R.id.registerCPassword);
        editText_Phone = (EditText) findViewById(R.id.registerPhone);
        radioBtnTerms = (RadioButton) findViewById(R.id.registerTermsRadioBtn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnGotoSignin = (Button) findViewById(R.id.btnBackSignin);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rg = (RadioGroup) findViewById(R.id.RG);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        btnGotoSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterScreen.this, SigninScreen.class);
                startActivity(intent);
            }
        });

    }


    // Function to toggle registration form enable(VISIBLE)/disable(GONE)
    public void toggleRegistrationFields(Integer visibility){
        editText_regFirstname.setVisibility(visibility);
        editText_regLastname.setVisibility(visibility);
        editText_regUsername.setVisibility(visibility);
        editText_regPassword.setVisibility(visibility);
        editText_regCPassword.setVisibility(visibility);
        editText_Phone.setVisibility(visibility);
        radioBtnTerms.setVisibility(visibility);
        btnRegister.setVisibility(visibility);
    }

    public void registerUser() {
        final String firstname = editText_regFirstname.getText().toString().trim();
        final String lastname = editText_regLastname.getText().toString().trim();
        final String username = editText_regUsername.getText().toString().trim();
        final String password = editText_regPassword.getText().toString().trim();
        String confirm_password = editText_regCPassword.getText().toString().trim();
        final String phone = editText_Phone.getText().toString().trim();

        int radioButton = rg.getCheckedRadioButtonId();
        rb = (RadioButton) findViewById(radioButton);
        final String type = rb.getText().toString().trim();

        // Check to make sure all the fields are filled-in
        if (firstname.isEmpty()) {
            editText_regFirstname.setError("Please enter your First Name");
            editText_regFirstname.requestFocus();
            return;
        }
        if (lastname.isEmpty()) {
            editText_regLastname.setError("Please enter your Last Name");
            editText_regLastname.requestFocus();
            return;
        }
        if (username.isEmpty()) { // Email should be Email
            editText_regUsername.setError("Please enter your username (Email Required)");
            editText_regUsername.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            editText_regUsername.setError("Please enter a valid email");
            editText_regUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editText_regPassword.setError("Please enter your password");
            editText_regPassword.requestFocus();
            return;
        }
        if (confirm_password.isEmpty()) {
            editText_regCPassword.setError("Please re-enter your password");
            editText_regCPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm_password)) {
            Toast.makeText(this.getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            editText_Phone.setError("Please enter your mobile number");
            editText_Phone.requestFocus();
            return;
        }
        if(phone.length() > 11){
            editText_Phone.setError("Minimum length of 11 digits");
            editText_Phone.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editText_regPassword.setError("Minimum length of 6 characters");
            return;
        }
        if (!radioBtnTerms.isChecked()) {
            Toast.makeText(this.getApplicationContext(), "You have to agree to the terms and conditions to proceed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        toggleRegistrationFields(View.GONE);
        progressBar.setVisibility(View.VISIBLE);


        // Sign up the new users
        mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // When task is complete
                if (task.isSuccessful()) {
                    // If the creation in is successful,
                    FirebaseUser newUser = task.getResult().getUser();

                    String id = newUser.getUid();

                    Users users = new Users(id, username, firstname, lastname, phone, type);

                    if (type.equals("USER")){

                    databaseRegisterUser.child(id).setValue(users);

                    final DatabaseReference dblocationref = FirebaseDatabase.getInstance().getReference("profiles")
                            .child(id).child("last_known_location").child("latitude");
                    final DatabaseReference dblocationref2 = FirebaseDatabase.getInstance().getReference("profiles")
                            .child(id).child("last_known_location").child("longitude");

                    dblocationref.setValue(0);
                    dblocationref2.setValue(0);

                    }
                    else{
                        databaseRegisterUser.child(id).setValue(users);

                        final DatabaseReference dblocationref = FirebaseDatabase.getInstance().getReference("profiles")
                                .child(id).child("last_known_location").child("latitude");
                        final DatabaseReference dblocationref2 = FirebaseDatabase.getInstance().getReference("profiles")
                                .child(id).child("last_known_location").child("longitude");
                        final DatabaseReference type = FirebaseDatabase.getInstance().getReference("profiles")
                                .child(id).child("status");
                        String pend = "Pending";
                        dblocationref.setValue(0);
                        dblocationref2.setValue(0);
                        type.setValue(pend);

                    }
                    Toast.makeText(getApplicationContext(), "Registration Successful",
                            Toast.LENGTH_SHORT).show();
                    updateDisplayName();

                    Handler reghandler = new Handler();

                    reghandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            final StorageReference file_storage = FirebaseStorage.getInstance().getReference("SelfiesWithValidID");

                            // This code block checks if there is a validation folder under the current user
                            // If there's no folder found, create a new folder for the validation picture
                            file_storage.child(current_user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Folder exists
                                    Intent intent = new Intent(RegisterScreen.this, SigninScreen.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Folder not found
                                    Intent intent = new Intent(RegisterScreen.this, SelfieValidation.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }, 2000); // END of reghandler


                } else {
                    // If sign in fails, display a message to the user.
                    progressBar.setVisibility(View.GONE);
                    toggleRegistrationFields(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Registration Failed",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Email already been used",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Update the profile of the user
    public void updateDisplayName(){
        // Declare and Initialize the user's first name and last name
        String fname = editText_regFirstname.getText().toString().trim();
        String lname = editText_regLastname.getText().toString().trim();
        final String displayName = fname+" "+lname; // Full name will be the display name

        final FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName).build();

            user.updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Welcome, "+
                                        user.getDisplayName(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, SigninScreen.class));
        finish();
    }
}
