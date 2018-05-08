package com.example.keima.ambulife;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class Sms extends AppCompatActivity {

    FirebaseUser user;
    final int isSent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);


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

                String strPhone = "09955338494";

                String strMessage = "Requesting medical help\n"
                        + "User ID: " + user.getUid() + "\n"
                        + "Longitude and Latitude: " + lng_marker + ", " + lat_marker;


                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(strPhone, null, strMessage, null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        finish();
    }


}
