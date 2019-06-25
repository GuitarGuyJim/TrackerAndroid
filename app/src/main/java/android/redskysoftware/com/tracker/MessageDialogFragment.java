package android.redskysoftware.com.tracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static MessageDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MESSAGE, message);

        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {

        String message = (String)getArguments().getSerializable(ARG_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //
        // Build the dialog with a Yes and Cancel button.  Events for the button will
        // be passed back to the listener.
        //
        builder.setMessage(message)
                .setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }
}
