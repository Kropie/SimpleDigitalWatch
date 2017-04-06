package com.kropiejohn.simpledigitalwatch.mobile;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.kropiejohn.simpledigitalwatch.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.Map;

/**
 * Created by jonat on 4/5/2017.
 */

public class ColorPickerDialogFragment extends DialogFragment {
    private String keyToUpdate;

    /**
     * Don't use this.
     */
    public ColorPickerDialogFragment () {
        // If key is not provided then assume that the background color will be modified.
    }

    public void setKeyToUpdate(String keyToUpdate) {
        this.keyToUpdate = keyToUpdate;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (keyToUpdate == null) {
            keyToUpdate = getString(R.string.background_color_key);
        }

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.color_picker_layout, null);
        final ColorPicker picker = initializeColorPicker(view);

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it will be going into a dialog layout
        builder.setView(view);
        builder.setPositiveButton(R.string.color_picker_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DigitalWatchColorConfig.getInstance().updateColorConfig(keyToUpdate, picker.getColor(), getActivity().getApplicationContext());
            }
        });

        builder.setNegativeButton(R.string.color_picker_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Don't do anything
            }
        });

        return builder.create();
    }

    private ColorPicker initializeColorPicker(View view) {
        ColorPicker picker = (ColorPicker) view.findViewById(R.id.color_picker);
        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.color_picker_saturation_bar);
        ValueBar valueBar = (ValueBar) view.findViewById(R.id.color_picker_value_bar);

        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        Map<String, Integer> colorMap = DigitalWatchColorConfig.getInstance().getColorMap();
        Integer currentValue = (colorMap.get(keyToUpdate) != null) ? colorMap.get(keyToUpdate) : Color.BLACK;
        picker.setOldCenterColor(currentValue);

        return picker;
    }
}
