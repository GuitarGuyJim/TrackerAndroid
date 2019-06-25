package android.redskysoftware.com.tracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Dialog that allows the user to input the name for a new track.  This class will add the new
 * track to the data model and inform the user of any errors.
 */
public class TrackNameFragment extends DialogFragment {

    /**
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface TrackNameFragmentListener {

        /**
         * Called when the positive button is clicked.
         * @param dialog  The dialog object
         * @param name  The name for the new track
         */
        void onDialogOkClick(DialogFragment dialog, String name);

        /**
         * Called when the cancel button is clicked on the dialog
         * @param dialog  The dialog object.
         */
        void onDialogCancelClick(DialogFragment dialog);
    }

    /** The text field that has the name for the new track */
    private EditText mEditText;

    /** The listener that will receive action events */
    private TrackNameFragmentListener mListener;

    /**
     *  Overrides the Fragment.onAttach() method to instantiate the TrackNameFragmentListener
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TrackNameFragmentListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString() + " must implement TrackNameFragmentListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceData) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dialog_new_track, null);
        mEditText = v.findViewById(R.id.new_track_name);

        //
        // Build the dialog with a Yes and Cancel button.  Events for the button will
        // be passed back to the listener.
        //
        builder.setView(v)
               .setTitle(R.string.track_name_title)
               .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = mEditText.getText().toString();
                                mListener.onDialogOkClick(TrackNameFragment.this, name);
                            }
                        })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mListener.onDialogCancelClick(TrackNameFragment.this);
                   }
               });

        return builder.create();
    }
}
