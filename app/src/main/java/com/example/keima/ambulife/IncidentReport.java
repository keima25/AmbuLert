package com.example.keima.ambulife;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

public class IncidentReport extends AppCompatActivity {

    TextView firstname_ems, ems_id, user_ID, date, lat, lng, firstname, lastname, lastname_ems;
    Button submit;
    EditText editText;
    FirebaseUser user;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_report);
        geocoder = new Geocoder(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firstname_ems = (TextView) findViewById(R.id.firstname_ems);
        ems_id = (TextView) findViewById(R.id.ems_id);
        user_ID = (TextView) findViewById(R.id.user_id);
        date = (TextView) findViewById(R.id.date);
        lat = (TextView) findViewById(R.id.lat);
        lng = (TextView) findViewById(R.id.lng);
        submit = (Button) findViewById(R.id.submit);
        editText = findViewById(R.id.editText);
        firstname = (TextView) findViewById(R.id.firstname);
        lastname = (TextView) findViewById(R.id.lastname);
        lastname_ems = (TextView) findViewById(R.id.lastname_ems);

        ems_id.setText(user.getUid());
        final String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("incidents");
        ref.orderByChild("ems").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("incidents").child(snap.getKey());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String id = dataSnapshot.child("ems").child("id").getValue(String.class);

                            if (id.equals(user.getUid())){
                                final DatabaseReference pend = FirebaseDatabase.getInstance().getReference("incidents").child(dataSnapshot.getKey());
                                final LatLng location;

                                final String url = dataSnapshot.child("image").getValue(String.class);

                                final String user_id = dataSnapshot.child("user").child("id").getValue(String.class);
                                final Double lat_user = dataSnapshot.child("user").child("location").child("latitude").getValue(Double.class);
                                NumberFormat nm_lat = NumberFormat.getNumberInstance();
                                final Double lng_user = dataSnapshot.child("user").child("location").child("longitude").getValue(Double.class);
                                NumberFormat nm_lng = NumberFormat.getNumberInstance();

                                location = new LatLng(lat_user, lng_user);

                                user_ID.setText(user_id);
                                lat.setText(nm_lat.format(lat_user));
                                lng.setText(nm_lng.format(lng_user));
                                date.setText(currentDateTimeString);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("profiles").child(user_id);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String firstname_user = dataSnapshot.child("firstname").getValue(String.class);
                                        final String lastname_user = dataSnapshot.child("lastname").getValue(String.class);

                                        firstname.setText(firstname_user);
                                        lastname.setText(lastname_user);

                                        final DatabaseReference dispatch = FirebaseDatabase.getInstance().getReference("profiles").child(user.getUid());
                                        dispatch.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(final DataSnapshot dataSnapshot) {

                                                final String ems_firstname = dataSnapshot.child("firstname").getValue(String.class);
                                                final String ems_lastname = dataSnapshot.child("lastname").getValue(String.class);

                                                firstname_ems.setText(ems_firstname);
                                                lastname_ems.setText(ems_lastname);

                                                submit.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        DatabaseReference toDB = FirebaseDatabase.getInstance().getReference("reports").push();
                                                        String text = editText.getText().toString().trim();
                                                        String address ="";

                                                        try {
                                                            List<Address> addressList;
                                                            addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                                                            address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                                                            address += addressList.get(0).getCountryName();
                                                        }
                                                        catch (IOException e){e.printStackTrace();}

                                                        toDB.child("address").setValue(address);
                                                        toDB.child("date").setValue(currentDateTimeString);
                                                        toDB.child("ems").child("firstname").setValue(ems_firstname);
                                                        toDB.child("ems").child("lastname").setValue(ems_lastname);
                                                        toDB.child("ems").child("id").setValue(user.getUid());
                                                        toDB.child("user").child("firstname").setValue(firstname_user);
                                                        toDB.child("user").child("lastname").setValue(lastname_user);
                                                        toDB.child("user").child("id").setValue(user_id);
                                                        toDB.child("location").child("latitude").setValue(lat_user);
                                                        toDB.child("location").child("longitude").setValue(lng_user);
                                                        toDB.child("remarks").setValue(text);
                                                        pend.child("status").setValue("completed");
                                                        dispatch.child("dispatching").setValue(false);
                                                        toDB.child("image").setValue(url);
                                                        toDB.child("user").child("dispatching").setValue(false);
                                                        toDB.child("user").child("responder").setValue(false);

                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                startActivity(new Intent(IncidentReport.this, MapsActivityEMS.class));
                                                            }
                                                        }, 3000);

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
