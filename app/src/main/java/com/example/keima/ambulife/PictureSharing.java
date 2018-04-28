package com.example.keima.ambulife;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureSharing extends AppCompatActivity {

    ImageButton imageButton;
    Button call;
    EditText editText;
    ProgressDialog progress;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference Storage;
    Uri PicUri;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference("TestPicture");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_sharing);

        imageButton = findViewById(R.id.imageButton);
        call = findViewById(R.id.call);
        editText = findViewById(R.id.editText);

        progress = new ProgressDialog(this);
        Storage = FirebaseStorage.getInstance().getReference();

        toggleButton("share");

    }

    private void toggleButton(String value){
        final String text = editText.getText().toString().trim();

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
                    progress.setMessage("Uploading");
                    progress.show();

                    Uri uri = PicUri;
                    StorageReference filepath = Storage.child("PhotoSharing").child(uri.getLastPathSegment());

                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progress.dismiss();
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            Picasso.get().load(downloadUri).fit().centerCrop().into(imageButton);


                            db.child("remark").setValue(text);
                            db.child("url").setValue(downloadUri).getResult();

                            Toast.makeText(PictureSharing.this, "Done Uploading", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            });
        }
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
