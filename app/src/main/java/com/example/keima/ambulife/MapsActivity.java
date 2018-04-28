package com.example.keima.ambulife;


import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private PermissionManager permissionManager;
    private Geocoder geocoder;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();

    FirebaseUser user;

    // Firebase User
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    // Firebase Database References
    DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference("profiles");
    DatabaseReference locationReference = profileReference.child(currentUser.getUid()).child("last_known_location");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Instantiate the class Geocoder
        geocoder = new Geocoder(getContext());


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statusCheck();

        // Initialize the map
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

    }


    private void updateLocationOnDatabase(DatabaseReference reference, LatLng value) {
        reference.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Location Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void statusCheck() {
        // Initialize Permission Manager
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(getActivity());
        // Check Location Service
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (mLocationManager != null && !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Your GPS/Location Service seems to be disabled. It is necessary that you enable this feature.")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);

        // Store permission results to String variable just to check
        ArrayList<String> granted = permissionManager.getStatus().get(0).granted;
        ArrayList<String> denied = permissionManager.getStatus().get(0).denied;

        // Permission Log
        for (String item : granted)
            Log.e("Permission Granted: ", item);

        for (String item : denied)
            Log.e("Permission Denied: ", item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMaxZoomPreference(17);
        subscribeToUpdates();
    }


    private void subscribeToUpdates() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("profiles")
                .child(user.getUid())
                .child("last_known_location");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key = dataSnapshot.getKey();
        double lat = dataSnapshot.child("latitude").getValue(Double.class);
        double lng = dataSnapshot.child("longitude").getValue(Double.class);
        LatLng location = new LatLng(lat, lng);
        if (!mMarkers.containsKey(key)) {

            // Add the marker and move the camera to the user coordinates
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
            try {
                List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                String address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                address += addressList.get(0).getCountryName();

                updateLocationOnDatabase(locationReference, location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            // Add the marker and move the camera to the user coordinates
            mMarkers.get(key).setPosition(location);
            try {
                List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                String address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                address += addressList.get(0).getCountryName();

                updateLocationOnDatabase(locationReference, location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }


    @Override
    public void onResume() {
        super.onResume();

        MapInterface mi = new MapInterface();
        mi.showCallButton();
    }

}
