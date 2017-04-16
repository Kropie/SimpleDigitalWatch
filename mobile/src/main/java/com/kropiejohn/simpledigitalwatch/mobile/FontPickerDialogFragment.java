package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.kropiejohn.simpledigitalwatch.R;
import com.kropiejohn.simpledigitalwatchface.util.FileUtil;
import com.kropiejohn.simpledigitalwatchface.FontView;
import com.kropiejohn.simpledigitalwatchface.util.TypeFaceUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Dialog Fragment to allow for the user to select a font. The list of fonts is generated from the
 * fonts in the fonts asset folder.
 * Created by jonat on 4/7/2017.
 */
public class FontPickerDialogFragment extends DialogFragment {
    private static final String TAG = "FontDialog";
    private Calendar mCalendar;
    private List<FontView> fontViewList;
    private Runnable mUpdateViewRunnable;
    private Handler mUpdateViewHandler;
    private LinearLayout mFontViewParentLayout;
    private View mInflatedView;
    private Dialog mDialog;
    private SharedPreferences mSharedPreferences;

    /**
     * Default constructor.
     */
    public FontPickerDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mCalendar = Calendar.getInstance();
        fontViewList = new ArrayList<>();

        builder.setView(getInflatedView());
        builder.setNegativeButton(getString(R.string.font_picker_negative_button_text), new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Don't do anything on cancel.
                    }
                });

        mDialog = builder.create();
        createFontViews();
        initializeUpdateTimer();
        return mDialog;
    }

    protected void initializeUpdateTimer() {
        mUpdateViewHandler = new Handler();
        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                updateAllFontViews();
                mUpdateViewHandler.postDelayed(this, 100);
            }
        };

        mUpdateViewHandler.postDelayed(mUpdateViewRunnable, 100);
    }

    protected void createFontViews() {
        try {
            String[] typeFaces = getActivity().getAssets().list(TypeFaceUtil.getFontsPath(this
                    .getActivity().getApplicationContext()));

            for (String strTypeface :
                    typeFaces) {
                initFontView(new FontView(getFontViewParentLayout().getContext()), strTypeface);
            }
            getFontViewParentLayout().invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFontView(final FontView fontView, String strTypeface) {
        try {
            initFontViewTypeface(fontView, strTypeface);
            updateFontViewText(fontView);
            initFontViewClickListener(fontView);
            setupFontViewLayoutParams(fontView);
            addFontViewToParentView(fontView);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }

    private void initFontViewTypeface(FontView fontView, String strTypeface) {
        fontView.setTypeface(TypeFaceUtil.createTypeFaceFromAssets(strTypeface, getActivity()
                .getApplicationContext()));
        fontView.setFont(strTypeface);
        fontView.setTextColor(Color.BLACK);
        // TODO add the text size to a resources file and use it here instead of this
        // hardcoded value.
        fontView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
    }

    private void initFontViewClickListener(final FontView fontView) {
        fontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                editor.putString(getString(R.string.time_typeface_key), fontView
                        .getFont());
                editor.apply();
                mUpdateViewHandler.removeCallbacks(mUpdateViewRunnable);
                mDialog.cancel();
            }
        });
    }

    private void addFontViewToParentView(FontView fontView) {
        getFontViewParentLayout().addView(fontView);
        fontViewList.add(fontView);
    }

    private void setupFontViewLayoutParams(FontView fontView) {
//        LinearLayout.LayoutParams fontViewLayoutParams = new LinearLayout.LayoutParams
//                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams
//                        .MATCH_PARENT);
//
//        fontView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        fontView.setLayoutParams(fontViewLayoutParams);
        fontView.setGravity(Gravity.CENTER);
    }

    private View getInflatedView() {
        if (mInflatedView == null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            mInflatedView = inflater.inflate(R.layout.font_picker_layout, null);
        }

        return mInflatedView;
    }

    private SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity()
                    .getApplicationContext());
        }

        return mSharedPreferences;
    }

    private void updateAllFontViews() {
        for (FontView fontView :
                fontViewList) {
            updateFontViewText(fontView);
        }
    }

    private LinearLayout getFontViewParentLayout() {
        if (mFontViewParentLayout == null) {
            mFontViewParentLayout = (LinearLayout) getInflatedView().findViewById(R.id
                    .ll_font_picker);
        }

        return mFontViewParentLayout;
    }

    private void updateFontViewText(FontView fontView) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd h:mm:ss", Locale.US);
        String updatedText = dateFormat.format(Calendar.getInstance().getTime());
        fontView.setText(updatedText);
    }
}
