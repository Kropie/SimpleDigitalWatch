package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    /**
     * Tag for logging purposes.
     */
    private static final String TAG = "FontDialog";

    /**
     * Holds all of the FontViews.
     */
    private List<FontView> fontViewList;

    /**
     * Runnable that will update the text for the font views.
     */
    private Runnable mUpdateViewRunnable;

    /**
     * Handler used to schedule the updates for the text for the font views.
     */
    private Handler mUpdateViewHandler;

    /**
     * LinearLayout that the font views will be placed inside of.
     */
    private LinearLayout mFontViewParentLayout;

    /**
     * Holds the inflated view for this dialog fragment.
     */
    private View mInflatedView;

    /**
     * Holds the dialog for this fragment.
     */
    private Dialog mDialog;

    /**
     * Holds the default shared preferences for the current application.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * Default constructor.
     */
    public FontPickerDialogFragment() {
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        createFontViews();
        builder.setView(getInflatedView());
        builder.setNegativeButton(getString(R.string.font_picker_negative_button_text), new
                DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Don't do anything on cancel.
                    }
                });

        mDialog = builder.create();
        initializeUpdateTimer();
        return mDialog;
    }

    /**
     * Initializes the runnable and handler that will be used to update the text for font views
     * to indicate the current time.
     */
    protected void initializeUpdateTimer() {
        final int updateTime = 10;
        mUpdateViewHandler = new Handler();
        mUpdateViewRunnable = new Runnable() {
            /** Holds current seconds value. Will be used to indicate if the FontViews should be
             * updated or not. */
            int currentSeconds;

            /** Holds reference to the android calendar. */
            Calendar calendar;

            @Override
            public void run() {
                if (calendar == null) {
                    calendar = Calendar.getInstance();
                }

                // Update the calendar to reflect the current time.
                calendar.setTimeInMillis(System.currentTimeMillis());

                // If the value of the seconds updated then update the text for the font views.
                if (currentSeconds != calendar.get(Calendar.SECOND)) {
                    currentSeconds = calendar.get(Calendar.SECOND);
                    updateAllFontViews();
                }

                mUpdateViewHandler.postDelayed(this, updateTime);
            }
        };
        mUpdateViewHandler.postDelayed(mUpdateViewRunnable, updateTime);
    }

    /**
     * Create the all of the FontViews. A FontView will be created for every font that is in the
     * fonts asset folder.
     */
    protected void createFontViews() {
        try {
            fontViewList = new ArrayList<>();
            String[] typeFaces = getActivity().getAssets().list(TypeFaceUtil.getFontsPath(this
                    .getActivity().getApplicationContext()));

            for (String strTypeface : typeFaces)
                initFontView(new FontView(getFontViewParentLayout().getContext()), strTypeface);

            getFontViewParentLayout().invalidate();
        } catch (IOException e) {
            Log.e(TAG, String.format(Locale.US, "createFontViews: Unable to create FontViews %s",
                    e));
        }
    }

    /**
     * Initializes necessary data/parameters for a font view.
     *
     * @param fontView    The FontView to initialize.
     * @param strTypeface The name of the typeface.
     */
    private void initFontView(final FontView fontView, String strTypeface) {
        try {
            initFontViewTypeface(fontView, strTypeface);
            updateFontViewText(fontView);
            initFontViewClickListener(fontView);
            setupFontViewLayoutParams(fontView);
            addFontViewToParentView(fontView);
        } catch (Exception e) {
            Log.e(TAG, String.format("initFontView: Unable to initialize FontView %s", e));
        }

    }

    /**
     * Initializes the Typeface for a given FontView.
     *
     * @param fontView    The FontView to update.
     * @param strTypeface The name of the Typeface.
     */
    private void initFontViewTypeface(FontView fontView, String strTypeface) {
        fontView.setTypeface(TypeFaceUtil.createTypeFaceFromAssets(strTypeface, getActivity()
                .getApplicationContext()));
        fontView.setFont(strTypeface);
        fontView.setTextColor(Color.BLACK);
        fontView.setTextSize(TypedValue.COMPLEX_UNIT_SP, R.integer.font_picker_font_size);
    }

    /**
     * Initializes OnClickListener for a given FontView.
     *
     * @param fontView FontView to add OnClickListener to.
     */
    private void initFontViewClickListener(final FontView fontView) {
        fontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences().edit()
                        .putString(getString(R.string.time_typeface_key), fontView.getFont())
                        .apply();
                mUpdateViewHandler.removeCallbacks(mUpdateViewRunnable);
                mDialog.cancel();
            }
        });
    }

    /**
     * @param fontView
     */
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
