package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.kropiejohn.simpledigitalwatch.R;
import com.kropiejohn.simpledigitalwatchface.FontView;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by jonat on 4/7/2017.
 */

public class FontPickerDialogFragment extends DialogFragment {
    private static final String TAG = "FontDialog";

    /**
     * Default constructor.
     */
    public FontPickerDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        View inflatedView = getInflatedView();
        builder.setView(inflatedView);

        builder.setNegativeButton(getString(R.string.font_picker_negative_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO provide implementation.
            }
        });

        Dialog dialog = builder.create();
        createFontViews(sharedPreferences, inflatedView, dialog);

        return dialog;
    }

    protected void createFontViews(final SharedPreferences sharedPreferences, View inflatedView, final Dialog dialog) {
        try {
            String fontsPath = getString(R.string.fonts_path);
            String[] typeFaces = getActivity().getAssets().list(fontsPath);
            LinearLayout linearLayout = (LinearLayout) inflatedView.findViewById(R.id.ll_font_picker);
            for (String strTypeface :
                    typeFaces) {
                final FontView fontView = new FontView(inflatedView.getContext());
                fontView.setText(getFontViewTextFromAssetName(strTypeface));
                fontView.setTextColor(Color.BLACK);
                try {
                    Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), fontsPath + "/" + strTypeface);
                    fontView.setTypeface(typeface);
                    fontView.setFont(strTypeface);
                    // TODO add the text size to a resources file and use it here instead of this hardcoded value.
                    fontView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                    fontView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    fontView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(getString(R.string.time_typeface_key), fontView.getFont());
                            editor.commit();
                            dialog.cancel();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                linearLayout.addView(fontView, params);
            }

            linearLayout.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFontViewTextFromAssetName(String fontAssetName) {
        String fontText = "";

        String[] assetNameParts = fontAssetName.split(Pattern.quote("."));
        if (assetNameParts.length == 0) {
            return fontAssetName;
        }

        String[] fontNameParts = assetNameParts[0].split(Pattern.quote("_"));
        for (String part:
             fontNameParts) {
            fontText = String.format(Locale.US, "%s %s", fontText, part);
        }

        return fontText;
    }

    private View getInflatedView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.font_picker_layout, null);
        return view;
    }
}
