package com.example.keima.ambulife;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;
import com.example.keima.ambulife.R;


public class VideoSharing extends AppCompatActivity implements Connector.IConnect{

    private boolean mVidyoClientInitialized = false;
    private Connector mVidyoConnector = null;
    private FrameLayout vidFrame;
//    Button startbtn, connectbtn, disconnectbtn;
    private String token = "cHJvdmlzaW9uAHVzZXIxQDMyMGVkMy52aWR5by5pbwA2MzY5MzY5NjQxMgAAZjIyYWNmNmQwMDE2MDQ0MjIwZmU3YjcwNDdiZTZjYThhYjM1ZDcxMGYxNzM0MDQyMDYxZDViZDVlY2M3NDIxOTQ5ODk1MzI4ZGI0YjMwMzAyMzM2M2VkM2E1NTNmNGU3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sharing);

        ConnectorPkg.setApplicationUIContext(this);
        mVidyoClientInitialized = ConnectorPkg.initialize();
        vidFrame = (FrameLayout)findViewById(R.id.videoFrame);
    }

    public void Start(View v){
        mVidyoConnector = new Connector(vidFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default,
                16, "", "", 0);

        mVidyoConnector.showViewAt(vidFrame, 0, 0, vidFrame.getWidth(), vidFrame.getHeight());
    }

    public void Connect(View v){
        Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();
        mVidyoConnector.connect("prod.vidyo.io", token, "user1", "Room", this);
    }

    public void Disconnect(View v){

        mVidyoConnector.disconnect();
        Toast.makeText(this, "Disconnecting", Toast.LENGTH_SHORT).show();
    }

    public void onSuccess() {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    public void onFailure(Connector.ConnectorFailReason connectorFailReason) {
        Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
    }

    public void onDisconnected(Connector.ConnectorDisconnectReason connectorDisconnectReason) {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Exit Video Chat?")
                .setCancelable(true)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
