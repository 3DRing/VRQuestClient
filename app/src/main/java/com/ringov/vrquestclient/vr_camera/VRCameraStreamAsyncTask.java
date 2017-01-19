package com.ringov.vrquestclient.vr_camera;

import android.os.AsyncTask;

import java.io.InputStream;

/**
 * Created by Сергей on 16.01.2017.
 */

public class VRCameraStreamAsyncTask extends AsyncTask<String, Void, VRCameraInputStream> {

    private VRCameraView left;

    VRCameraStreamAsyncTask(VRCameraView left){
        this.left = left;
    }

    @Override
    protected VRCameraInputStream doInBackground(String... params) {
        VRCameraInputStream stream = VRCameraInputStream.read(params[0]);
        return stream;
    }

    @Override
    protected void onPostExecute(VRCameraInputStream stream) {
        if(stream != null) {
            this.left.setSource(stream);
        }
    }
}