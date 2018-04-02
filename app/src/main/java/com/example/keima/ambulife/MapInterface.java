package com.example.keima.ambulife;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MapInterface extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private android.support.v7.widget.Toolbar appbar;
    FloatingActionButton fab;
    ProgressBar progressBar;
    NavigationView nav_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_interface);

        //Firebase Objects Declarations
        mAuth = FirebaseAuth.getInstance();

        //Get appBar ID
        appbar = (android.support.v7.widget.Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Floating action button id
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
                Toast.makeText(getApplicationContext(), "You have pressed the button", Toast.LENGTH_SHORT).show();
            }
        });

        // Map Fragment on Start
        Fragment fragment = null;
        fragment = new MapsActivity();
        if(fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        int menuId = item.getItemId();

        if(menuId == R.id.nav_profile){
            Toast.makeText(this.getApplicationContext(), "You clicked Profile", Toast.LENGTH_SHORT).show();
            fragment = new ProfileFragment();
        }

        else if(menuId == R.id.nav_settings){
            Toast.makeText(this.getApplicationContext(), "You clicked Settings", Toast.LENGTH_SHORT).show();
        }


        else if(menuId == R.id.nav_logout){
            Toast.makeText(this.getApplicationContext(), "You clicked Logout", Toast.LENGTH_SHORT).show();
        }

        if(fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.screen_area, fragment);
            ft.commit();
        }

        mDrawerLayout.closeDrawers();
        return false;
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
