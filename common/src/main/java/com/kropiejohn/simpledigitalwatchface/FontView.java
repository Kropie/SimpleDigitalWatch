package com.kropiejohn.simpledigitalwatchface;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;

/**
 * Created by jonat on 4/1/2017.
 */

public class FontView extends AppCompatTextView {
    private String font;

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public FontView(Context context) {
        super(context);
    }
}
