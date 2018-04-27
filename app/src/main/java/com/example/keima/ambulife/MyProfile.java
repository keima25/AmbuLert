package com.example.keima.ambulife;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView closeBtn, profileImageView, imageView_edit_name, imageView_edit_address, imageView_edit_age,
            imageView_edit_gender, imageView_edit_mobileNum;
    private TextView emailView, nameView, homeaddressView, ageView, genderView, phoneView;
    private CardView nameView_card, homeaddressView_card, ageView_card, genderView_card, phoneView_card;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    AlertDialog editDialog;
    EditText editText, editText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Instantiate Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get current user
        currentUser = mAuth.getCurrentUser();

        MapInterface.fab.setVisibility(View.GONE);

        // View Declarations
        closeBtn = findViewById(R.id.closeButton);

        profileImageView = findViewById(R.id.profile_imageView);
        emailView = findViewById(R.id.profile_emailAddress);
        nameView = findViewById(R.id.profile_name);
        homeaddressView = findViewById(R.id.profile_homeAddress);
        ageView = findViewById(R.id.profile_age);
        genderView = findViewById(R.id.profile_gender);
        phoneView = findViewById(R.id.profile_phoneNum);

        nameView_card = findViewById(R.id.name_card);
        homeaddressView_card = findViewById(R.id.homeAddress_card);
        ageView_card = findViewById(R.id.age_card);
        genderView_card = findViewById(R.id.gender_card);
        phoneView_card = findViewById(R.id.profile_phoneNum_card);

        imageView_edit_name = findViewById(R.id.imageView_edit_name);
        imageView_edit_address = findViewById(R.id.imageView_edit_address);
        imageView_edit_age = findViewById(R.id.imageView_edit_age);
        imageView_edit_gender = findViewById(R.id.imageView_edit_gender);
        imageView_edit_mobileNum = findViewById(R.id.imageView_edit_mobileNum);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        displayProfile();

        // EDIT PROFILE FUNCTIONS
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });

        imageView_edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(MyProfile.this).create();
                editText = new EditText(MyProfile.this);
                editText2 = new EditText(MyProfile.this);

                // Get the database reference for the field to update
                final DatabaseReference firstnameRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("firstname");
                final DatabaseReference lastnameRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("lastname");

                // Set the Dialog title and view
                editDialog.setTitle("Edit name");
                editDialog.setView(editText); // First name;

                editDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(editText.getText())){
                            editText.setError("Please enter a value");
                        }else{
                            updateProfile(firstnameRef, editText.getText().toString().trim());
                            refreshActivity();
                        }
                    }
                });
                editDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editDialog.cancel();
                    }
                });

                // GET the value of the reference and set for editText
                firstnameRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        editText.setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Error fetching value:", databaseError.toException());
                    }
                });

                //Show the Edit Dialog
                editDialog.show();
            }
        });

        imageView_edit_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(MyProfile.this).create();
                editText = new EditText(MyProfile.this);

                // Get the database reference for the field to update
                final DatabaseReference genderRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("gender");

                // Set the title and View of the dialog
                editDialog.setTitle("Edit gender");
                editDialog.setView(editText);

                editDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(editText.getText())){
                            editText.setError("Please enter a value");
                        }else{
                            updateProfile(genderRef, editText.getText().toString().trim());
                            refreshActivity();
                        }
                    }
                });
                editDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editDialog.cancel();
                    }
                });

                // GET the value of the reference and set for editText
                genderRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        editText.setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Error fetching value:", databaseError.toException());
                    }
                });

                // Show the Edit Dialog
                editDialog.show();
            }
        });

        imageView_edit_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(MyProfile.this).create();
                editText = new EditText(MyProfile.this);

                // Get the database reference for the field to update
                final DatabaseReference ageRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("age");

                // Set the title and View of the dialog
                editDialog.setTitle("Edit age");
                editDialog.setView(editText);

                editDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(editText.getText())){
                            editText.setError("Please enter a value");
                        }else{
                            updateProfile(ageRef, editText.getText().toString().trim());
                            refreshActivity();
                        }
                    }
                });
                editDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editDialog.cancel();
                    }
                });

                // GET the value of the reference and set for editText
                ageRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        editText.setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Error fetching value:", databaseError.toException());
                    }
                });

                // Set the value for editText
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editDialog.show();
            }
        });

        imageView_edit_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(MyProfile.this).create();
                editText = new EditText(MyProfile.this);

                // Get the database reference for the field to update
                final DatabaseReference homeAddressRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("homeaddress");

                // Set the title and View of the dialog
                editDialog.setTitle("Edit address");
                editDialog.setView(editText);

                editDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(editText.getText())){
                            editText.setError("Please enter a value");
                        }else{
                            updateProfile(homeAddressRef, editText.getText().toString().trim());
                            refreshActivity();
                        }
                    }
                });
                editDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editDialog.cancel();
                    }
                });

                // GET the value of the reference and set for editText
                homeAddressRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        editText.setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Error fetching value:", databaseError.toException());
                    }
                });

                // Set the value for editText
                editText.setText(homeaddressView.getText());
                editDialog.show();
            }
        });

        imageView_edit_mobileNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(MyProfile.this).create();
                editText = new EditText(MyProfile.this);

                // Get the database reference for the field to update
                final DatabaseReference phoneRef = FirebaseDatabase.getInstance()
                        .getReference("profiles").child(currentUser.getUid()).child("phone");

                // Set the title and View of the dialog
                editDialog.setTitle("Edit mobile number");
                editDialog.setView(editText);

                editDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(TextUtils.isEmpty(editText.getText())){
                            editText.setError("Please enter a value");
                        }else{
                            updateProfile(phoneRef, editText.getText().toString().trim());
                            refreshActivity();
                        }
                    }
                });
                editDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editDialog.cancel();
                    }
                });

                // GET the value of the reference and set for editText
                phoneRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        editText.setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Error fetching value:", databaseError.toException());
                    }
                });

                // Set the value for editText
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editDialog.show();
            }
        });

        //When close button is clicked
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void refreshActivity() {
        finish();
        startActivity(getIntent());
    }

    private void updateProfile(DatabaseReference reference, String value){
        reference.setValue(value)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return;
    }

    private void getReferenceValue(DatabaseReference reference){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Error fetching value:", databaseError.toException());
            }
        });
    }

    private void displayProfile() {

        final DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference("profiles").child(currentUser.getUid());

        dbreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String f = dataSnapshot.child("firstname").getValue(String.class);
                String l = dataSnapshot.child("lastname").getValue(String.class);
                nameView.setText(f+" "+l);
                emailView.setText(dataSnapshot.child("email").getValue(String.class));
                genderView.setText(dataSnapshot.child("gender").getValue(String.class));
                    ageView.setText(dataSnapshot.child("age").getValue(String.class));
                    homeaddressView.setText(dataSnapshot.child("homeaddress").getValue(String.class));
                    phoneView.setText(dataSnapshot.child("phone").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
