package com.example.keima.ambulife;

import android.app.Activity;
import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private PermissionManager permissionManager;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private boolean mLocationPermissionsGranted = false;
    LatLng coordinates;
    ProgressDialog dialog;

    // Firebase User
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    // Firebase Database References
    DatabaseReference profileReference = FirebaseDatabase.getInstance().getReference("profiles");
//    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Current Calls");
    DatabaseReference locationReference = profileReference.child(currentUser.getUid()).child("last_known_location");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_maps, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Permission Manager
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(getActivity());
        statusCheck();

        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        // Initialize the map
        initMap();
        dialog = ProgressDialog.show(getContext(), "",
                "Getting your location. Please wait...", true);

        // Checks if the location permissions are not granted
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        dialog.show();

        // Check if the Network Provider is enabled
        // If True: Proceed to location listener
        if (mLocationManager.isProviderEnabled(mLocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Get location latitude and longitude
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    // Instantiate the class LatLng
                    LatLng mycoordinates = new LatLng(lat, lng);
                    coordinates = mycoordinates;
                    // Instantiate the class Geocoder
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                        String address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                        address += addressList.get(0).getCountryName();
                        // Add the marker and move the camera to the user coordinates
                        mMap.addMarker(new MarkerOptions().position(mycoordinates).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mycoordinates, 15.0f));
                        dialog.hide();
                        updateLocationOnDatabase(locationReference, mycoordinates);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (mLocationManager.isProviderEnabled(mLocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Get location latitude and longitude
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    // Instantiate the class LatLng
                    LatLng mycoordinates = new LatLng(lat, lng);
                    coordinates = mycoordinates;
                    // Instantiate the class Geocoder
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                        String address = addressList.get(0).getSubLocality() + ", " + addressList.get(0).getLocality() + ",";
                        address += addressList.get(0).getCountryName();
                        // Add the marker and move the camera to the user coordinates
                        mMap.addMarker(new MarkerOptions().position(mycoordinates).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mycoordinates, 15.0f));
                        dialog.hide();
                        updateLocationOnDatabase(locationReference, mycoordinates);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    private void updateLocationOnDatabase(DatabaseReference reference, LatLng value){
        reference.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Location Updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void statusCheck() {
        mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(mLocationManager.GPS_PROVIDER)) {
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
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialog, final int id) {
//                        dialog.cancel();
//                    }
//                });
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

    public void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(getActivity(), "Map is Ready", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onMapReady: map is ready");
    }

    private void refreshFragment() {
        // Reload current fragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

//    public void getLocationPermission(){
//        Log.d(TAG, "getLocationPermission: getting location permissions");
//        String[] permimissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
//
//        if (ContextCompat.checkSelfPermission(getActivity(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            if (ContextCompat.checkSelfPermission(getActivity(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                mLocationPermissionsGranted = true;
//                initMap();
//            }else{
//                ActivityCompat.requestPermissions(getActivity(), permimissions, LOCATION_PERMISSION_REQUEST_CODE );
//            }
//
//        }else{
//            ActivityCompat.requestPermissions(getActivity(), permimissions, LOCATION_PERMISSION_REQUEST_CODE );
//        }
//    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        Log.d(TAG,"onRequestPermissionsResult: called");
//        mLocationPermissionsGranted = false;
//
//        switch (requestCode){
//            case LOCATION_PERMISSION_REQUEST_CODE:{
//                if (grantResults.length > 0){
//                    for (int i = 0; i<grantResults.length;i++){
//                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
//                            mLocationPermissionsGranted = false;
//                            Log.d(TAG,"onRequestPermissionsResult: permission failed");
//                            return;
//                        }
//                    }
//                    Log.d(TAG,"onRequestPermissionsResult: permission granted");
//                    mLocationPermissionsGranted = true;
//
//                    initMap();
//                }
//            }
//        }
//    }


}
