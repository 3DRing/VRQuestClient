package com.ringov.vrquestclient.vr_camera;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ringov.vrquestclient.R;

public class VRCameraStreamActivity extends AppCompatActivity {

    private VRCameraView left;
    private SurfaceView right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // only fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_vrcamara_stream);

        left = (VRCameraView) findViewById(R.id.leftView);
        right = (SurfaceView) findViewById(R.id.rightView);

        left.init(this, right);
        left.setDisplayMode(VRCameraView.SIZE_BEST_FIT);
        left.showFps(false);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url"); // TODO remove hardcoded text
        if (url != null) {

            VRCameraStreamAsyncTask task = new VRCameraStreamAsyncTask(left);
            task.execute(url);
        } else {
            Toast.makeText(this, "Stream doesn't exist", Toast.LENGTH_LONG).show();
        }
    }

    public void onPause() {
        super.onPause();
        left.stopPlayback();
    }

    // TODO move to separate place
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
