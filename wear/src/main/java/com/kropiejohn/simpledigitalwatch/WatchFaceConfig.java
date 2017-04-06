package com.kropiejohn.simpledigitalwatch;

import android.content.Context;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by jonat on 3/30/2017.
 */

public interface WatchFaceConfig {
    void updateConfig(DataMap updatedPreferences, Context cxt);

    int getBackgroundColor();

    int getForegroundColor();
}
