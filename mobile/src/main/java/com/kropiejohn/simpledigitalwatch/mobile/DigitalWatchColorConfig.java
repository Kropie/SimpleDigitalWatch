package com.kropiejohn.simpledigitalwatch.mobile;

import android.content.Context;

import com.kropiejohn.simpledigitalwatch.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonat on 4/5/2017.
 */

public class DigitalWatchColorConfig extends WatchColorConfig {

    private Map<String, Integer> colorMap;

    private DigitalWatchColorConfig() {
        super(new Object());
        colorMap = new HashMap<>();
    }

    @Override
    void updateColorConfig(String key, Integer colorValue, Context context) {
        if(key == null || context == null || colorValue == null) {
            return;
        }

        Integer oldValue = null;
        if (key.equals(context.getString(R.string.background_color_key))) {
            oldValue = (colorMap.containsKey(key)) ? colorMap.get(key) : oldValue;
            colorMap.put(context.getString(R.string.background_color_key), colorValue);
            firePropertyChange(key, oldValue, colorValue);
        } else if (key.equals(context.getString(R.string.foreground_color_key))) {
            oldValue = (colorMap.containsKey(key)) ? colorMap.get(key) : oldValue;
            colorMap.put(context.getString(R.string.foreground_color_key), colorValue);
            firePropertyChange(key, oldValue, colorValue);
        }
    }

    public Map<String, Integer> getColorMap() {
        return colorMap;
    }

    public static DigitalWatchColorConfig getInstance() {
        return Singleton.SINGLETON.getConfig();
    }

    private enum Singleton {
        SINGLETON(new DigitalWatchColorConfig());
        DigitalWatchColorConfig config;

        private Singleton(DigitalWatchColorConfig config) {
            this.config = config;
        }

        public DigitalWatchColorConfig getConfig() {
            return config;
        }
    }
}
