package com.kropiejohn.simpledigitalwatch.mobile;

import android.content.Context;

import java.beans.PropertyChangeSupport;

/**
 * Singleton class for holding watch configuration data.
 */
public abstract class WatchColorConfig extends PropertyChangeSupport {

    protected WatchColorConfig(Object sourceBean) {
        super(sourceBean);
    }

    abstract void updateColorConfig(String key, Integer colorValue, Context context);
}
