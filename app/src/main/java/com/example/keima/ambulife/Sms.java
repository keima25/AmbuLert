package com.example.keima.ambulife;

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

                String strPhone = "09369572668";

                String strMessage = "Longitude and Latitude = "+ lng_marker + " " + lat_marker;

                SmsManager sms = SmsManager.getDefault();

                sms.sendTextMessage(strPhone, null, strMessage, null, null);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "Sent.", Toast.LENGTH_SHORT).show();


    }
}
