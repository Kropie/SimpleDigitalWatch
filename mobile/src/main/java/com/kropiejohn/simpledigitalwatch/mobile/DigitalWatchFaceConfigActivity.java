package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Typeface;
import android.net.Uri;
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class DigitalWatchFaceConfigActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<DataApi.DataItemResult>, PropertyChangeListener {

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    private String TAG = "ConfigActivity";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_watch_face_config);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        ComponentName name = getIntent().getParcelableExtra(
                WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);

        setupListeners();

        try {
            String fontsPath = getString(R.string.fonts_path);
            String[] typeFaces = this.getAssets().list(fontsPath);
            for (String strTypeface :
                    typeFaces) {
                final FontView fontView = new FontView(this);
                fontView.setText(strTypeface);
                try {
                    Typeface typeface = Typeface.createFromAsset(this.getAssets(), fontsPath + "/" + strTypeface);
                    fontView.setTypeface(typeface);
                    fontView.setFont(strTypeface);
                    // TODO add the text size to a resources file and use it here instead of this hardcoded value.
                    fontView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                    fontView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    fontView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendConfigUpdateMessage(getString(R.string.time_typeface_key), fontView.getFont());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_digital_watch_Config);
                linearLayout.addView(fontView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(getString(R.string.background_color_key)) ||
                evt.getPropertyName().equals(getString(R.string.background_color_key))) {
            Integer value = (Integer) evt.getNewValue();
            sendConfigUpdateMessage(evt.getPropertyName(), value);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        DigitalWatchColorConfig.getInstance().removePropertyChangeListener(this);
        super.onStop();
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

    private void setupListeners() {
        Button selectColorButton = (Button) findViewById(R.id.selectColorButton);
        selectColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogFragment colorPickerFragment = new ColorPickerDialogFragment();
                colorPickerFragment.show(DigitalWatchFaceConfigActivity.this.getFragmentManager(), "");
                colorPickerFragment.setKeyToUpdate(getString(R.string.background_color_key));
            }
        });

        DigitalWatchColorConfig.getInstance().addPropertyChangeListener(this);
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


}
