package com.kropiejohn.simpledigitalwatch;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.common.api.GoogleApiClient;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

/**
 * Created by jonat on 3/31/2017.
 */
public class DigitalWatchFaceConfigListener extends WatchFaceConfigListener {

    private final String TAG = "ConfigListener";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        byte[] rawData = messageEvent.getData();
        DataMap configKeysToOverwrite = DataMap.fromByteArray(rawData);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onMessageReceived(MessageEvent) - DataMap contains \n" + configKeysToOverwrite);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(DigitalWatchFaceConfigListener.this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult =
                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "onMessageReceived: Failed to connect to GoogleApiClient");
//                return;
            }
        }

        DigitalWatchFaceConfig.getInstance().updateConfig(configKeysToOverwrite, this.getApplicationContext());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + i);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + connectionResult);
        }
    }
}
