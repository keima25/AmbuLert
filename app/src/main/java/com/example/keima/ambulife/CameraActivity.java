package com.example.keima.ambulife;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by Steven on 13/04/2018.
 */

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 0;

    ImageView cameraView;
    Button cameraButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initViews();

    }

    private void initViews() {
        cameraView = (ImageView) findViewById(R.id.cameraView);
        cameraButton = (Button) findViewById(R.id.cameraBtn);
    }

}
