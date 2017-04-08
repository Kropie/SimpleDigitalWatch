package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.kropiejohn.simpledigitalwatch.R;
import com.kropiejohn.simpledigitalwatchface.FontView;

import java.io.IOException;

// TODO clean up class to remove clutter that was created by basing this class on the example project.
public class DigitalWatchFaceConfigActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<DataApi.DataItemResult>, SharedPreferences.OnSharedPreferenceChangeListener {

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    private String TAG = "ConfigActivity";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_watch_face_config_layout);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        ComponentName name = getIntent().getParcelableExtra(
                WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);

        initializeViews();
        setupSharedPreferences();
    }

    private void initializeViews() {
        Button selectColorButton = (Button) findViewById(R.id.selectBackgroundColorButton);
        selectColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogFragment colorPickerFragment = new ColorPickerDialogFragment();
                colorPickerFragment.show(DigitalWatchFaceConfigActivity.this.getFragmentManager(), "");
                colorPickerFragment.setKeyToUpdate(getString(R.string.background_color_key));
            }
        });

        Button selectFontButton = (Button)  findViewById(R.id.selectTimeFontButton);

        selectFontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fontFragment = new FontPickerDialogFragment();
                fontFragment.show(DigitalWatchFaceConfigActivity.this.getFragmentManager(), "");
            }
        });
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + bundle);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null || sharedPreferences == null)
            return;

        if(key.equals(getString(R.string.background_color_key))) {
            sendConfigUpdateMessage(key, sharedPreferences.getInt(key, Color.BLACK));
        } else if (key.equals(getString(R.string.foreground_color_key))) {
            sendConfigUpdateMessage(key, sharedPreferences.getInt(key, Color.WHITE));
        } else if (key.equals(getString(R.string.time_typeface_key))) {
            sendConfigUpdateMessage(key, sharedPreferences.getString(key, null));
        }
    }

    // TODO Figure out what I should be doing here.
    // https://developer.android.com/samples/WatchFace/Application/src/com.example.android.wearable.watchface/DigitalWatchFaceCompanionConfigActivity.html
    @Override
    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + i);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + connectionResult);
        }
    }

    private void sendConfigUpdateMessage(String configKey, int color) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, color);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + Integer.toHexString(color));
            }
        }
    }

    private void sendConfigUpdateMessage(String configKey, String update) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, update);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + update);
            }
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }
}
