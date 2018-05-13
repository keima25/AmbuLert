package com.example.keima.ambulife;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PictureSharing extends AppCompatActivity {

    ImageButton imageButton;
    Button call;
    EditText editText;
    ProgressDialog progress;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String EMERGENCY_NUMBER = "09283948082";
    private StorageReference Storage;
    Uri PicUri;
    FirebaseUser user;
    DatabaseReference ongoingCallRef = FirebaseDatabase.getInstance().getReference("ongoing_calls");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_sharing);

        user = FirebaseAuth.getInstance().getCurrentUser();

        imageButton = findViewById(R.id.imageButton);
        call = findViewById(R.id.call);
        editText = findViewById(R.id.editText);

        progress = new ProgressDialog(this);
        Storage = FirebaseStorage.getInstance().getReference();

        toggleButton("share");

    }

    private void toggleButton(String value){
//        final String text = ;

        if (value == "share"){
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getOutputMediaFile(1);
                    PicUri = FileProvider.getUriForFile(PictureSharing.this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, PicUri);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
            });
        }
        else if(value == "upload"){
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    call(EMERGENCY_NUMBER);

//                    progress.setMessage("Uploading");
//                    progress.show();

                }
            });
        }
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

                        // Send message
                        startActivity(new Intent(PictureSharing.this, Sms.class));
                        startCallSession();
                        startActivity(call_intent);
                    }
                });

        // Show Alert Dialog
        final AlertDialog callAlertDialog = builder.create();
        callAlertDialog.show();

    }

    private void startCallSession() {

        final DatabaseReference profile = FirebaseDatabase.getInstance().getReference("profiles").child(user.getUid());

        // Get current timestamp
        Long tsLong = System.currentTimeMillis() / 1000;
        final String timestamp = tsLong.toString();

        SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy/hh:mm:ss");
        final String format = s.format(new Date());
//        final Random random = new Random();
//        final long call_id = random.nextInt( 9999999 - 1 + 1) + 1;


        // Get current location from database
        profile.child("last_known_location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final LatLng latLng = new LatLng(
                        dataSnapshot.child("latitude").getValue(Double.class),
                        dataSnapshot.child("longitude").getValue(Double.class)
                );

                final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("ongoing_calls").child(user.getUid());

                dbref.child("call_last_known_location").setValue(latLng).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call:", "Saved Location" + latLng);
                    }
                });

                dbref.child("call_date_time").setValue(format).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call", "Saved datetime");
                    }
                });

                dbref.child("call_timestamp").setValue(timestamp).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call", "Saved timestamp");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        Uri uri = PicUri;
        StorageReference filepath = Storage.child("PhotoSharing").child(user.getUid())
                .child(uri.getLastPathSegment());

        final DatabaseReference dbref2 = FirebaseDatabase.getInstance().getReference("ongoing_calls").child(user.getUid());

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            progress.dismiss();
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                Picasso.get().load(downloadUri).fit().centerCrop().into(imageButton);


                dbref2.child("remarks").setValue(editText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call", "Saved remarks");
                    }
                });
                dbref2.child("call_type").setValue("emergency").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call", "Saved call_type");
                    }
                });
                dbref2.child("image_url").setValue(String.valueOf(downloadUri)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("On_Call", "Saved image_url");
                    }
                });

                Toast.makeText(PictureSharing.this, "Done Uploading", Toast.LENGTH_LONG).show();

            }
        });
    }

    private File getOutputMediaFile(int type){
        File mediaStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Application");

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
                    .into(imageButton);
            Toast.makeText(PictureSharing.this, "Picture Taken", Toast.LENGTH_LONG).show();
            toggleButton("upload");
        }
    }

}
