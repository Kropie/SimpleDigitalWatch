package com.kropiejohn.simpledigitalwatch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import com.google.android.gms.wearable.DataMap;

import java.beans.PropertyChangeListener;
import java.util.Map;

/**
 * Created by jonat on 3/30/2017.
 */

public interface WatchFaceConfig {
    void updateConfig(DataMap updatedPreferences, Context cxt);

    int getBackgroundColor();

    int getForegroundColor();
}
