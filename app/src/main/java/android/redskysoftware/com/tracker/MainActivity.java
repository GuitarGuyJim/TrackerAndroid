package android.redskysoftware.com.tracker;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * The MainActivity is the activity is run after the user logs in. It contains buttons that allow
 * the user to start a new track or view existing tracks.
 */
public class MainActivity extends AppCompatActivity
                          implements TrackNameFragment.TrackNameFragmentListener {

    private static final String NEW_TRACK_DIALOG_TAG = "DialogNewTrack";

    /** The "New Track" button */
    private Button mNewButton;

    /** The "History" button */
    private Button mHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        // Get the New Track button and add a click listener to it.
        //
        mNewButton = findViewById(R.id.new_track_button);
        mNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ActiveTrackActivity.class);
                startActivity(intent);

                //
                // Display the New Track Name dialog to allow the user to enter a
                // name for the track.  The dialog will create the track.
                //
            //    FragmentManager manager = getSupportFragmentManager();
            //    TrackNameFragment dialog = new TrackNameFragment();
            //    dialog.show(manager, NEW_TRACK_DIALOG_TAG);
            }
        });

        //
        // Get the History button and add a click listener to it
        //
        mHistoryButton = findViewById(R.id.history_button);
        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * Implements the TrackNameFragmentListener method.  Called when the user Ok button is clicked.
     * @param dialog  The dialog object
     * @param name  The name for the new track
     */
    public void onDialogOkClick(DialogFragment dialog, String name) {

        //
        // Check that the name is valid (the length is at least one character in length).
        //
        int length = name.length();
        if (length == 0) {
            MessageDialogFragment msgDialog = MessageDialogFragment.newInstance("Invalid name");
            FragmentManager manager = getSupportFragmentManager();
            msgDialog.show(manager, "MESSAGE_DIALOG");
        }
    }

    /**
     * Implements the TrackNameFragmentListener method.  Called when the cancel button is
     * clicked on the dialog
     * @param dialog  The dialog object.
     */
    public void onDialogCancelClick(DialogFragment dialog) {
        //
        // User pressed the cancel button while entering the name for a new track.
        // Nothing to do; the dialog will close.
    }
}


