package com.ringov.vrquestclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.ringov.vrquestclient.vr_camera.VRCameraStreamActivity;

public class MainActivity extends AppCompatActivity {

    private String url = "http://192.168.0.102:8090";
    private Button btn;
    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // only fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        et = (EditText) findViewById(R.id.et);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = "http://" + et.getText().toString();

                Intent intent = new Intent(MainActivity.this, VRCameraStreamActivity.class);
                intent.putExtra("url", url); // TODO remove hardcoded string
                startActivity(intent);
            }
        });


    }
}