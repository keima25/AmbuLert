package com.example.keima.ambulife;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.vidyo.VidyoClient.Endpoint.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapsActivityEMS extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, DirectionCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private PermissionManager permissionManager;
    private Geocoder geocoder;
    private static final String TAG = MapsActivityEMS.class.getSimpleName();
    private HashMap<String, Marker> assignUserMarkers = new HashMap<>();
    private HashMap<String, Marker> mMarkers = new HashMap<>();

    public static Activity mapInterface;
    private int start = 0;
    private int queryCount = 0;

    boolean dispatch = false;
    boolean respond = false;
    String USER_ID = "";
    FloatingActionButton fabMyLocationCamera, fabMyLocationInfo, fabReport;
    TextView etaTextView, distanceTextView;

    FirebaseUser user;

    private String serverKey = "AIzaSyBFOWoDbj5nMYzO1VUmPwVQZCeRgB5m4Ik";
    private LatLng origin = new LatLng(0.0, 0.0);
    private LatLng destination = new LatLng(0.0, 0.0);
    private LatLng origin2 = new LatLng(0.0, 0.0);

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

        start = 1;
        fabMyLocationCamera = mapInterface.findViewById(R.id.fabMyLocation);
        fabMyLocationInfo = mapInterface.findViewById(R.id.fabLocationInfo);
        fabReport = mapInterface.findViewById(R.id.fab);
        etaTextView = mapInterface.findViewById(R.id.etaTextView);
        distanceTextView = mapInterface.findViewById(R.id.distanceTextView);


        statusCheck();

//        LatLng origin1, destination1;
//
//        origin1 = new LatLng();
//        destination1 = new LatLng();
//
//
//        GoogleDirection.withServerKey(serverKey)
//                .from(origin1)
//                .to(destination1)
//                .unit(Unit.METRIC)
//                .transportMode(TransportMode.DRIVING)
//                .execute(MapsActivityEMS.this);

//        Toast.makeText(getContext(), "EMS MAP", Toast.LENGTH_SHORT).show();

        // Initialize the map
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivityEMS.this);

    }


    private void updateLocationOnDatabase(DatabaseReference reference, LatLng value) {
        reference.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    Toast.makeText(getContext(), "Location Updated", Toast.LENGTH_SHORT).show();
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
        mMap.setMaxZoomPreference(18);
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
                checkAssignedUser(); // Check if there is assigned USER to USER_ID

                Log.i("USERID = ", USER_ID);

                if(USER_ID != ""){
                    showReport();
                    requestDirection(USER_ID);
                    setMarkerAssignUser(USER_ID);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showReport(){
        fabReport.show();
        fabReport.setImageResource(R.drawable.ic_report);
        fabReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), IncidentReport.class));
            }
        });
    }


    private void setMarker(DataSnapshot dataSnapshot) {

        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key;
        double lat;
        double lng;
        final LatLng location;
        String address = "";

        key = dataSnapshot.getKey();
        lat = dataSnapshot.child("latitude").getValue(Double.class);
        lng = dataSnapshot.child("longitude").getValue(Double.class);
        location = new LatLng(lat, lng);


        if (!mMarkers.containsKey(key)) {

            try {
                List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                address += addressList.get(0).getCountryName();

                updateLocationOnDatabase(locationReference, location);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add the marker and move the camera to the user coordinates
            mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(address)
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_ems_marker))));

            final String finalAddress = address;
            fabMyLocationInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogLocationInfo(finalAddress, location);
                }
            });

        } else {

            // Get the marker from mMarkers and move the camera to the user coordinates
            mMarkers.get(key).setPosition(location);
        }

        origin = location;

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

        if (start == 1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
            start = 0;
        }

        fabMyLocationCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
            }
        });
        mMap.setOnMarkerClickListener(this);

    }

    // Dialog for Location information
    private void dialogLocationInfo(String address, LatLng location) {
        Log.i("ADDRESS: ", address);

        // Initialize dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("My Location Info \n" + address +
                "\nLatitude, Longitude: \n" + location.toString())
                .setCancelable(true)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                    }
                });

        // Show Alert Dialog
        final AlertDialog callAlertDialog = builder.create();
        callAlertDialog.show();
    }

    // This is to check if a Service is running
    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service already", "running");
                return true;
            }
        }
        Log.i("Service not", "running");
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        MapInterface mi = new MapInterface();

        mi.hideCallButton();

        if (!isMyServiceRunning(TrackerService.class, getContext())) {
            getContext().startService(new Intent(getContext(), TrackerService.class));
        }
    }

    // This override method is executed once a marker has been clicked
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("MARKER ID:", marker.getId());
        Log.i("MARKER TITLE", marker.getTitle());

        String address = marker.getTitle();
        LatLng position = marker.getPosition();


        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("INFO\n" + position + "\n" + address + "\nVideo call with this user?")
                .setCancelable(true)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(getContext(), VideoSharing.class));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

        return false;
    }

    public void readData(DatabaseReference ref, final MapsActivity.OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }

        });
    }

    public void checkAssignedUser() {

        final DatabaseReference incidents = FirebaseDatabase.getInstance().getReference("incidents");
        incidents.orderByChild("status").equalTo("pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snap : dataSnapshot.getChildren()){ // Incident Key

                    Log.i("incident key", snap.getKey());

                    DatabaseReference incidentRef = FirebaseDatabase.getInstance().getReference("incidents")
                            .child(snap.getKey());// Reference to incidents / incident key

                    readData(incidentRef, new MapsActivity.OnGetDataListener() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            String id = dataSnapshot.child("ems").child("id").getValue(String.class);
                            Log.i("ID = ", dataSnapshot.child("ems").child("id").getValue(String.class));
                            if(id.equals(currentUser.getUid())){
                                Log.i("Status: ", "Dispatch");
                                USER_ID = dataSnapshot.child("user").child("id").getValue(String.class);
                                dispatch = true;
//                                Toast.makeText(getContext(), "DANGER", Toast.LENGTH_SHORT).show();

                                Double lat, lng;
                                lat = dataSnapshot.child("user").child("location").child("latitude").getValue(Double.class);
                                lng = dataSnapshot.child("user").child("location").child("longitude").getValue(Double.class);

                                LatLng location = new LatLng(lat, lng);

                                Log.i("LOCATION", location.toString());
//                                destination = location;
                                Log.d(TAG, "origin: " + origin.toString());
                                Log.d(TAG, "destination: " + destination.toString());

                            }
                            else{
                                Log.i("Status: ", "SAFE");
                            }
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setMarkerAssignUser(String userID) {
//        emsMarkers.clear();

        DatabaseReference userprofile = FirebaseDatabase.getInstance().getReference("profiles").child(userID)
                .child("last_known_location");

        userprofile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String key;
                double lat_marker;
                double lng_marker;
                final LatLng location;
                String address = "";

                key = dataSnapshot.getKey();
                lat_marker = dataSnapshot.child("latitude").getValue(Double.class);
                lng_marker = dataSnapshot.child("longitude").getValue(Double.class);
                location = new LatLng(lat_marker, lng_marker);

                if (!assignUserMarkers.containsKey(key)) { // If location is not saved yet. Get location update and set marker
                    try {
                        List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                        address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                        address += addressList.get(0).getCountryName();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Add the marker and move the camera to the user coordinates
                    assignUserMarkers.put(key, mMap.addMarker(new MarkerOptions().title(address).position(location)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_user_marker))));


                } else { // If location is already saved. Set marker

                    // Get the marker from mMarkers and move the camera to the user coordinates
                    assignUserMarkers.get(key).setPosition(location);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestDirection(String user_id) {

        DatabaseReference userloc = FirebaseDatabase.getInstance().getReference("profiles").child(user_id).child("last_known_location");

        readData(userloc, new MapsActivity.OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Double lat = dataSnapshot.child("latitude").getValue(Double.class);
                Double lng = dataSnapshot.child("longitude").getValue(Double.class);

                LatLng location = new LatLng(lat, lng);

                destination = location;

                GoogleDirection.withServerKey(serverKey)
                        .from(origin)
                        .to(destination)
                        .unit(Unit.METRIC)
                        .transportMode(TransportMode.DRIVING)
                        .execute(MapsActivityEMS.this);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
        });
    }



    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        Log.d(TAG, "origin: " + origin.toString());
        Log.d(TAG, "destination: " + destination.toString());
//        Toast.makeText(getActivity(), direction.getStatus(), Toast.LENGTH_LONG).show();
        if (direction.isOK()) {
            queryCount++;
            Log.i("QUERY COUNT = ", String.valueOf(queryCount));

            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);


            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();

            mMap.addPolyline(DirectionConverter.createPolyline(getContext(), directionPositionList, 5, Color.RED));

            Info distanceInfo = leg.getDistance();
            Info durationInfo = leg.getDuration();
            String distance = distanceInfo.getText();
            String duration = durationInfo.getText();

            Log.d(TAG, "distance: " + distance);
            Log.d(TAG, "duration: " + duration);

            etaTextView.setText("Estimated Travel Time:\n"+duration.toString());
            distanceTextView.setText("Distance from Responder:\n"+distance.toString());


        } else if(direction.getStatus().equals("OVER_QUERY_LIMIT")){


            Log.i("DIRECTION STATUS = ", "OVER_QUERY_LIMIT");
            Toast.makeText(getContext(), direction.getStatus(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getContext(), direction.getStatus(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
    }


}
