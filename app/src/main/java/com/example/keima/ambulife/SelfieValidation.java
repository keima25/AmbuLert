package com.example.keima.ambulife;

import android.animation.LayoutTransition;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.LoginFilter;
import android.transition.ChangeBounds;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.xml.sax.ErrorHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SelfieValidation extends AppCompatActivity {

    ImageButton cameraBtn;
    Button submit, cancel;
    TextView skip;
    LinearLayout buttonViews, nextStepScreen, verificationScreen ;
    Uri PicUri;
    ProgressDialog progressDialog;
    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_validation);

        cameraBtn = (ImageButton) findViewById(R.id.selfieCameraBtn);
        submit = (Button) findViewById(R.id.submitBtn);
        cancel = (Button) findViewById(R.id.cancelBtn);
        skip = (TextView) findViewById(R.id.skipText);
        buttonViews = (LinearLayout) findViewById(R.id.actionView);
        nextStepScreen = (LinearLayout) findViewById(R.id.nextStepScreen);
        verificationScreen = (LinearLayout) findViewById(R.id.verificationScreen);

        progressDialog = new ProgressDialog(this);

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference status = FirebaseDatabase.getInstance().getReference("profiles").child(current_user.getUid());

        status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").exists()){
                    if(dataSnapshot.child("status").getValue(String.class).equals("Pending")){
                        skip.setVisibility(View.GONE);
                }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // Set onClickListener for the first screen: nextStepScreen
        nextStepScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                transitionScreen();
                return true;
            }
        });

        // Set onClickListener for the Skip button
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelfieValidation.this, TrackerActivity.class));
                finish();
            }
        });



        // Set click function for the camera button
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initiate Device Camera via Intent
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getOutputMediaFile(1);
                PicUri = FileProvider.getUriForFile(SelfieValidation.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, PicUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        // Set click function for Action Buttons

        // Submit Button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri uri = PicUri;
                final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("profiles");
                final StorageReference file_storage = FirebaseStorage.getInstance().getReference("SelfiesWithValidID");
                final String storage_file_name = "Selfie_Validation_"+current_user.getUid();

                progressDialog.setMessage("Uploading");
                progressDialog.show();

                // This code block checks if there is a validation folder under the current user
                // If there's no folder found, create a new folder for the validation picture
                file_storage.child(current_user.getUid()).child(storage_file_name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Folder exists
                        progressDialog.dismiss();
                        Toast.makeText(SelfieValidation.this, "Validated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Folder not found
                        // Save imageUrl to user profile

                        //Create new folder > new image file
                        StorageReference newFolderReference = FirebaseStorage.getInstance().getReference("SelfiesWithValidID")
                                .child(current_user.getUid())
                                .child(storage_file_name);

                        newFolderReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();

                                DatabaseReference profile = FirebaseDatabase.getInstance().getReference("profiles")
                                        .child(current_user.getUid());

                                profile.child("validation_image").setValue(String.valueOf(downloadUri)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.i("UPLOAD: ", PicUri.toString());
                                    }
                                });


                                Toast.makeText(SelfieValidation.this, "Photo successfully uploaded. Thank you for validating your account."
                                        ,Toast.LENGTH_LONG).show();

                                profile.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.child("status").getValue(String.class).equals("Pending")){
                                            progressDialog.dismiss();
                                            Toast.makeText(SelfieValidation.this, "Thank you. Please wait for 5 minutes for us to validate your account.", Toast.LENGTH_SHORT).show();
                                            Log.i("Status: ", "Pending");

                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();

                                            mAuth.signOut();

                                            startActivity(new Intent(SelfieValidation.this, RegisterScreen.class));
                                            finish();
                                        }else{
                                            Log.i("Status: ", "Verified");
                                            progressDialog.dismiss();
                                            startActivity(new Intent(SelfieValidation.this, TrackerActivity.class));
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


                    }
                });

            }
        });

        // Cancel Button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicUri = null;
                cameraBtn.setImageResource(R.drawable.ic_menu_camera);
                buttonViews.setVisibility(View.GONE);
            }
        });
    }

    // Create Interface for Callbacks
    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
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


    private void transitionScreen() {
        TransitionManager.beginDelayedTransition(nextStepScreen);

        final android.support.transition.ChangeBounds transition;
        transition = new android.support.transition.ChangeBounds();
        transition.setDuration(1000L); // Sets a duration of 600 milliseconds

        nextStepScreen.setVisibility(View.GONE);

    }

    private File getOutputMediaFile(int type) {
        File mediaStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Ambulert_Images");

        if (!mediaStorage.exists()){
            if (!mediaStorage.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorage.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        }else {
            return null;
        }
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uri = PicUri;
            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(cameraBtn);
            Toast.makeText(SelfieValidation.this, "Picture Taken", Toast.LENGTH_LONG).show();
            buttonViews.setVisibility(View.VISIBLE);
        }
    }
}
