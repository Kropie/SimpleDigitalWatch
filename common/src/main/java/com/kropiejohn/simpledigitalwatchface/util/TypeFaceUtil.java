package com.kropiejohn.simpledigitalwatchface.util;

import android.content.Context;
import android.graphics.Typeface;

import com.kropiejohn.simpledigitalwatchfacecommon.R;

/**
 * Collection of utility methods for Typeface objects.
 * Created by jonat on 4/16/2017.
 */

public class TypeFaceUtil {

    public static Typeface createTypeFaceFromAssets(String fontFileName, Context context) {
        return Typeface.createFromAsset(context.getAssets(),
                FileUtil.joinPaths(getFontsPath(context), fontFileName));
    }

    public static String getFontsPath(Context context) {
        return context.getString(R.string.fonts_path);
    }
}
