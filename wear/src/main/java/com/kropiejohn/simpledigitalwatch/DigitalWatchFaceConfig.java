package com.kropiejohn.simpledigitalwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by jonat on 3/30/2017.
 */
public class DigitalWatchFaceConfig implements WatchFaceConfig {

    private Integer backgroundColor;

    private Integer foregroundColor;

    private String TAG = "DigitalConfig";

    private DigitalWatchFaceConfig() {
    }

    @Override
    public void updateConfig(DataMap dataMap, Context cxt) {
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
    public int getBackgroundColor() {
        if (backgroundColor == null) {
            backgroundColor = Color.BLACK;
        }
        return backgroundColor;
    }

    @Override
    public int getForegroundColor() {
        if (foregroundColor == null) {
            foregroundColor = Color.WHITE;
        }
        return foregroundColor;
    }

    public static DigitalWatchFaceConfig getInstance() {
        return Singleton.INSTANCE.getConfig();
    }

    private enum Singleton {
        INSTANCE(new DigitalWatchFaceConfig());

        private DigitalWatchFaceConfig config;

        Singleton(DigitalWatchFaceConfig config) {
            this.config = config;
        }

        public DigitalWatchFaceConfig getConfig() {
            return config;
        }

    }
}
