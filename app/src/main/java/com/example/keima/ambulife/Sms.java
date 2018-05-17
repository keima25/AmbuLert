package com.example.keima.ambulife;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class Sms extends AppCompatActivity {

    FirebaseUser user;
    final int isSent = 0;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        geocoder = new Geocoder(getApplicationContext());

        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("profiles")
                .child(user.getUid())
                .child("last_known_location");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double lat_marker;
                double lng_marker;
                lat_marker = dataSnapshot.child("latitude").getValue(Double.class);
                lng_marker = dataSnapshot.child("longitude").getValue(Double.class);

                // Phone number to send the message
                String strPhone = "09269384310"; // ron

//                String address = "";

//                String strPhone = "09495945171"; // al
//                String strPhone = "09426658102"; // john

                // Get the formatted address through geocoding
//                try {
//                    List<Address> addressList = geocoder.getFromLocation(lat_marker, lng_marker, 1);
//                    address = addressList.get(0).getAddressLine(0) +", "
//                            +addressList.get(0).getSubLocality() + ", "
//                            +addressList.get(0).getLocality() + ","
//                            +addressList.get(0).getCountryName();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


                // Message Body
                String strMessage = "Requesting medical help!\n"
                        + "User ID: " + user.getUid() + "\n"
                        + "\nLatitude and Longitude: " + lat_marker + ", " + lng_marker +"\n"
                        + "\nEmail: "+ user.getEmail();


                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(strPhone, null, strMessage, null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
        finish();
    }


}
