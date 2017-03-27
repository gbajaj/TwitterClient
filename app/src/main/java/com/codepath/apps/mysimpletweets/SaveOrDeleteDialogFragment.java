package com.codepath.apps.mysimpletweets;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by gauravb on 3/23/17.
 */

public class SaveOrDeleteDialogFragment extends DialogFragment {
    public SaveOrDeleteDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // set dialog icon
                .setIcon(android.R.drawable.stat_notify_error)
                // set Dialog Title
                .setTitle("Save Tweet?")
                // Set Dialog Message
                .setMessage("Do you want to save this tweet?")

                // positive button
                .setPositiveButton("Save",
                        (dialog, which) -> {
                            Toast.makeText(getActivity(), "Pressed OK", Toast.LENGTH_SHORT).show();
                        })
                // negative button
                .setNegativeButton("Discard",
                        (dialog, which) -> {
                            Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
                        }).create();
    }


}
