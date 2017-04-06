package com.kropiejohn.simpledigitalwatch;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

/**
 * Created by jonat on 4/5/2017.
 */

public class ColorPickerDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it will be going into a dialog layout
        builder.setView(inflater.inflate(R.layout.color_picker_layout, null));
        builder.setPositiveButton(R.string.color_picker_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do something
            }
        });

        builder.setNegativeButton(R.string.color_picker_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do something
            }
        });

        return builder.create();
    }
}
