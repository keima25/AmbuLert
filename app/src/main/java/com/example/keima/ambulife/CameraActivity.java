package com.example.keima.ambulife;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


public class CameraActivity extends AppCompatActivity {

    ImageView cameraView;
    Button cameraButton;
    ProgressDialog progress;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference Storage;
    Uri PicUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraButton = findViewById(R.id.cameraBtn);
        cameraView = findViewById(R.id.cameraView);
        progress = new ProgressDialog(this);
        Storage = FirebaseStorage.getInstance().getReference();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = getOutputMediaFile(1);
            PicUri = FileProvider.getUriForFile(CameraActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, PicUri);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
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
            progress.setMessage("Uploading");
            progress.show();

            Uri uri = PicUri;
            StorageReference filepath = Storage.child("Photos").child(uri.getLastPathSegment());

            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progress.dismiss();
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    Picasso.get().load(downloadUri).fit().centerCrop().into(cameraView);
                    Toast.makeText(CameraActivity.this, "Done Uploading", Toast.LENGTH_LONG).show();
                }
            });
            }

        }
    }

