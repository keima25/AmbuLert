package com.example.keima.ambulife;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

import java.sql.Timestamp;
import java.util.ArrayList;

public class MapInterface extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionManager permissionManager;
    private FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private android.support.v7.widget.Toolbar appbar;
    public static FloatingActionButton fab;
    private final static String EMERGENCY_NUMBER = "1234567890";
    private TextView nav_view_email, nav_view_name;
    ProgressBar progressBar;
    NavigationView nav_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_interface);

        // Initialize Permission Manager
        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);

        //Firebase Objects Declarations
        mAuth = FirebaseAuth.getInstance();

        //Get appBar ID
        appbar = (android.support.v7.widget.Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Floating action button id. This serves as the call button
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //Get drawerLayout ID
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_nav, R.string.close_nav);

        //Get NaviagationView id
        nav_view = (NavigationView) findViewById(R.id.nav_view);

        // Assign drawer listener
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nav_view.setNavigationItemSelectedListener(this);



        // Floating action button function onCLick
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                call(EMERGENCY_NUMBER);
                startActivity(new Intent(getApplicationContext(), PictureSharing.class));
            }
        });



        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference typeRef = FirebaseDatabase.getInstance().getReference("profiles").child(user.getUid()).child("type");

        readData(typeRef, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Fragment fragment = null;

                if(dataSnapshot.getValue(String.class).equals("USER"))
                { fragment = new MapsActivity();}
                else
                { fragment = new MapsActivityEMS();}


                pd.dismiss();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                ft.replace(R.id.screen_area, fragment);
                ft.commit();
            }

            @Override
            public void onStart() {
                //when starting
                Log.d("ONSTART", "Started");
                Toast.makeText(MapInterface.this, "Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                Log.d("onFailure", "Failed");
                Toast.makeText(MapInterface.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Create Interface for Callbacks
    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }

    public void readData(DatabaseReference ref, final OnGetDataListener listener) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        int menuId = item.getItemId();

        if (menuId == R.id.nav_profile) {
            Intent i = new Intent(this, MyProfile.class);
            startActivity(i);
        } else if (menuId == R.id.nav_settings) {
//            Toast.makeText(this.getApplicationContext(), "You clicked Settings", Toast.LENGTH_SHORT).show();
        } else if (menuId == R.id.nav_logout) {
            signOutUser();
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }

        mDrawerLayout.closeDrawers();
        return false;
    }

//    public void call(String number) {
//        final String callnumber = number;
//
//        // Initialize dialog
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Your are about to call 911. Please note that your location will be automatically detected.")
//                .setCancelable(false)
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialog, final int id) {
//                        dialog.cancel();
//                    }
//                })
//                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialog, final int id) {
//                        // Create a new Intent for the call service
//                        Intent call_intent = new Intent(Intent.ACTION_CALL);
//                        call_intent.setData(Uri.parse("tel:" + callnumber));
//
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//                        startCallSession();
//                        startActivity(call_intent);
//                    }
//                });
//
//        // Show Alert Dialog
//        final AlertDialog callAlertDialog = builder.create();
//        callAlertDialog.show();
//
//    }

//    private void startCallSession() {
//
//        final FirebaseUser currentUser = mAuth.getCurrentUser();
//        DatabaseReference profile = FirebaseDatabase.getInstance().getReference("profiles").child(currentUser.getUid());
//
//        // Get current timestamp
//        Long tsLong = System.currentTimeMillis() / 1000;
//        final String timestamp = tsLong.toString();
//
//        // Get current location from database
//        profile.child("last_known_location").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                double lat = dataSnapshot.child("latitude").getValue(Double.class);
//                double lng = dataSnapshot.child("longitude").getValue(Double.class);
//                final LatLng curLocation = new LatLng(lat, lng);
//
//                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ongoing_calls").child(currentUser.getUid());
//
//                dbref.child("caller_id").setValue(currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.i("On_Call:", "Saved Caller Id");
//                    }
//                });
//
//                dbref.child("call_timestamp").setValue(timestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.i("On_Call", "Saved timestamp");
//                    }
//                });
//
//                dbref.child("call_last_known_location").setValue(curLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.i("On_Call:", "Saved Location" + curLocation);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//        });
//    }


    // This method checks checks everytime the service is stopped while in app
    // and starts it again
//    private void updateService(String action) {
//        final Handler mUpdater = new Handler();
//        Runnable mUpdateView = new Runnable() {
//            @Override
//            public void run() {
//                if (!isMyServiceRunning(TrackerService.class, getApplicationContext())) {
//                    startService(new Intent(getApplicationContext(), TrackerService.class));
//                }
//                mUpdater.postDelayed(this, 3000);
//            }
//        };
//
//        if(action == "start")
//        { mUpdateView.run();}
//
//        if(action == "stop")
//        {
//            stopService(new Intent(this, TrackerService.class));
//            mUpdater.removeCallbacks(mUpdateView);
//        }
//    }

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

    // Sign out user
    public void signOutUser() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Logging out");
        pd.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms
                // Sign the user out
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "You've signed out",
                        Toast.LENGTH_LONG).show();
                pd.dismiss();

                Intent intent = new Intent(MapInterface.this, SigninScreen.class);
                startActivity(intent);
            }
        }, 5000);
    }

    public void showCallButton() {
        fab.show();
    }


    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You are leaving Ambulert. Continue exiting the app?")
                .setCancelable(true)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
//                        updateService("stop");
                        stopService(new Intent(MapInterface.this, TrackerService.class));
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MapInterface.this, TrackerService.class));
    }

}
