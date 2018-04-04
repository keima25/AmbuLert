package com.example.keima.ambulife;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Steven on 02/04/2018.
 */

public class ProfileFragment extends Fragment {

    ImageView closeBtn;
    TextView nameView, homeaddressView, ageView, genderView, phoneView;
    CardView nameView_card, homeaddressView_card, ageView_card, genderView_card, phoneView_card;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference databaseRef;
    GestureDetector gd;

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


        //When close button is clicked
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Change back to MapsActivity Fragment
                Fragment fragment = null;
                fragment = new MapsActivity();
                if(fragment != null){
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    MapInterface.fab.setVisibility(View.VISIBLE);
                    ft.replace(R.id.screen_area, fragment);
                    ft.commit();
                }
            }
        });

        displayProfile();

        gd = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return super.onDoubleTapEvent(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return super.onDown(e);
            }
        });

        nameView_card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gd.onTouchEvent(motionEvent);
            }
        });





    }

    private void displayProfile(){
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("profiles");
        String curUserID = currentUser.getUid();

        nameView.setText(currentUser.getDisplayName());
        phoneView.setText(currentUser.getUid());
    }

}
