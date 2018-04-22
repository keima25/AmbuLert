package com.example.keima.ambulife;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class MapInterface extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionManager permissionManager;
    private FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private android.support.v7.widget.Toolbar appbar;
    public static FloatingActionButton fab;
    private final static String EMERGENCY_NUMBER = "1234567890";
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
                call(EMERGENCY_NUMBER);
            }
        });

        // Map Fragment on Start
        Fragment fragment = null;
        fragment = new MapsActivity();
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }


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
            Toast.makeText(this.getApplicationContext(), "You clicked Profile", Toast.LENGTH_SHORT).show();
            fragment = new ProfileFragment();
        } else if (menuId == R.id.nav_settings) {
            Toast.makeText(this.getApplicationContext(), "You clicked Settings", Toast.LENGTH_SHORT).show();
        } else if (menuId == R.id.nav_logout) {
            Toast.makeText(this.getApplicationContext(), "You clicked Logout", Toast.LENGTH_SHORT).show();
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

    public void call(String number) {
        final String callnumber = number;

        // Initialize dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your are about to call 911. Please note that your location will be automatically detected.")
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    // Create a new Intent for the call service
                        Intent call_intent = new Intent(Intent.ACTION_CALL);
                        call_intent.setData(Uri.parse("tel:" + callnumber));

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(call_intent);
                    }
                });

        // Show Alert Dialog
        final AlertDialog callAlertDialog = builder.create();
        callAlertDialog.show();

    }

    // Sign out user
    public void signOutUser(){
//        progressBar.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms
                // Sign the user out
                mAuth.signOut();
                Toast.makeText(getApplicationContext(), "You've signed out",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MapInterface.this, SigninScreen.class);
                startActivity(intent);
            }
        }, 5000);
    }
}
