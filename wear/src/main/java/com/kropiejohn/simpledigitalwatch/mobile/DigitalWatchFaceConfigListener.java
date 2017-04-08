package com.kropiejohn.simpledigitalwatch.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kropiejohn.simpledigitalwatch.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by jonat on 3/31/2017.
 */
public class DigitalWatchFaceConfigListener extends WatchFaceConfigListener {

    private final String TAG = "ConfigListener";

    private GoogleApiClient mGoogleApiClient;

    private int backgroundColor;

    private int foregroundColor;

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

        updateConfig(configKeysToOverwrite);
    }

    public void updateConfig(DataMap dataMap) {
        Context cxt = this.getApplicationContext();

        String backgroundKey = cxt.getString(R.string.background_color_key);
        String foregroundKey = cxt.getString(R.string.foreground_color_key);
        String timeTypeFaceKey = cxt.getString(R.string.time_typeface_key);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (dataMap.containsKey(backgroundKey)) {
            backgroundColor = dataMap.getInt(backgroundKey);
            editor.putInt(backgroundKey, backgroundColor);
        }

        if (dataMap.containsKey(foregroundKey)) {
            foregroundColor = dataMap.getInt(foregroundKey);
            editor.putInt(backgroundKey, foregroundColor);
        }

        if (dataMap.containsKey(timeTypeFaceKey)) {
            String typeFaceAsset = dataMap.getString(timeTypeFaceKey);
            editor.putString(timeTypeFaceKey, typeFaceAsset);
        }

        editor.commit();
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
