package com.example.keima.ambulife;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.lang.UCharacter;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Steven on 02/04/2018.
 */

public class ProfileFragment extends Fragment {

    private ImageView closeBtn, profileImageView, imageView_edit_name, imageView_edit_address, imageView_edit_age,
            imageView_edit_gender, imageView_edit_mobileNum;
    private TextView emailView, nameView, homeaddressView, ageView, genderView, phoneView;
    private CardView nameView_card, homeaddressView_card, ageView_card, genderView_card, phoneView_card;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    AlertDialog editDialog;
    EditText editText, editText2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, null);

    }


    // Do everything
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instantiate Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        MapInterface.fab.setVisibility(View.GONE);

        // View Declarations
        closeBtn = (ImageView) view.findViewById(R.id.closeButton);

        profileImageView = (ImageView) view.findViewById(R.id.profile_imageView);
        emailView = (TextView) view.findViewById(R.id.profile_emailAddress);
        nameView = (TextView) view.findViewById(R.id.profile_name);
        homeaddressView = (TextView) view.findViewById(R.id.profile_homeAddress);
        ageView = (TextView) view.findViewById(R.id.profile_age);
        genderView = (TextView) view.findViewById(R.id.profile_gender);
        phoneView = (TextView) view.findViewById(R.id.profile_phoneNum);

        nameView_card = (CardView) view.findViewById(R.id.name_card);
        homeaddressView_card = (CardView) view.findViewById(R.id.homeAddress_card);
        ageView_card = (CardView) view.findViewById(R.id.age_card);
        genderView_card = (CardView) view.findViewById(R.id.gender_card);
        phoneView_card = (CardView) view.findViewById(R.id.profile_phoneNum_card);

        imageView_edit_name = (ImageView) view.findViewById(R.id.imageView_edit_name);
        imageView_edit_address = (ImageView) view.findViewById(R.id.imageView_edit_address);
        imageView_edit_age = (ImageView) view.findViewById(R.id.imageView_edit_age);
        imageView_edit_gender = (ImageView) view.findViewById(R.id.imageView_edit_gender);
        imageView_edit_mobileNum = (ImageView) view.findViewById(R.id.imageView_edit_mobileNum);

        //When close button is clicked
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change back to MapsActivity Fragment
                Fragment fragment = null;
                fragment = new MapsActivity();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    MapInterface.fab.setVisibility(View.VISIBLE);
                    ft.replace(R.id.screen_area, fragment);
                    ft.commit();
                }
            }
        });

        displayProfile();


        // EDIT PROFILE FUNCTIONS

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

        imageView_edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog = new AlertDialog.Builder(getActivity()).create();
                editText = new EditText(getActivity());
                editText2 = new EditText(getActivity());

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
                            refreshFragment();
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
                editDialog = new AlertDialog.Builder(getActivity()).create();
                editText = new EditText(getActivity());

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
                            refreshFragment();
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
                editDialog = new AlertDialog.Builder(getActivity()).create();
                editText = new EditText(getActivity());

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
                            refreshFragment();
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
                editDialog = new AlertDialog.Builder(getActivity()).create();
                editText = new EditText(getActivity());

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
                            refreshFragment();
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
                editDialog = new AlertDialog.Builder(getActivity()).create();
                editText = new EditText(getActivity());

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
                            refreshFragment();
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

    }

    private void updateProfile(DatabaseReference reference, String value){
        reference.setValue(value)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_LONG).show();
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
        currentUser = mAuth.getCurrentUser();

        databaseRef = FirebaseDatabase.getInstance().getReference("profiles");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    String f = snap.child("firstname").getValue(String.class);
                    String l = snap.child("lastname").getValue(String.class);
                    nameView.setText(f+" "+l);
                    emailView.setText(snap.child("email").getValue(String.class));
                    genderView.setText(snap.child("gender").getValue(String.class));
                    ageView.setText(snap.child("age").getValue(String.class));
                    homeaddressView.setText(snap.child("homeaddress").getValue(String.class));
                    phoneView.setText(snap.child("phone").getValue(String.class));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void refreshFragment(){
        // Reload current fragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
}
