package com.example.keima.ambulife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;


public class VideoSharing extends AppCompatActivity implements Connector.IConnect{

    private boolean mVidyoClientInitialized = false;
    private Connector mVidyoConnector = null;
    private FrameLayout vidFrame;
    private String token = "cHJvdmlzaW9uAHVzZXIxQDMyMGVkMy52aWR5by5pbwA2MzY5MjcxMDE3NAAAYjI3ZDc1Nzk2YmI3ODVhNzg2NzM0NmJmN2Y4YmQ2YzhmYmZiYWZlMDUwZDNmZDNkYWY2NzVjZGU4NDIwZDNmMTdmOWVhYzFkMGE2ZWYzZjg0OWZhYTAxZWE1NTA5MTJk";

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
        mVidyoConnector.connect("prod.vidyo.io", token, "user1", "Room", this);
    }

    public void Disconnect(View v){
        mVidyoConnector.disconnect();
    }

    public void onSuccess() {

    }

    public void onFailure(Connector.ConnectorFailReason connectorFailReason) {

    }

    public void onDisconnected(Connector.ConnectorDisconnectReason connectorDisconnectReason) {

    }
}