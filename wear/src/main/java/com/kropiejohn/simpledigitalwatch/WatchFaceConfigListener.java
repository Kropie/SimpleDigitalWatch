package com.kropiejohn.simpledigitalwatch;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.common.api.GoogleApiClient;

import java.beans.PropertyChangeListener;

/**
 * Created by jonat on 3/31/2017.
 */
public abstract class WatchFaceConfigListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
}
