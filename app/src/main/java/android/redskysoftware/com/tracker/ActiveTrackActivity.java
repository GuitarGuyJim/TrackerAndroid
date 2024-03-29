package android.redskysoftware.com.tracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActiveTrackActivity extends AppCompatActivity
                                 implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final String KEY_RUNNING = "RUNNING";
    private static final String KEY_ELAPSED_TIME = "ELAPSED_TIME";

    /**
     * The data model
     */
    private TrackerDataModel mModel;

    /**
     * The start/stop button
     */
    private Button mStartButton;

    /**
     * Flag to keep track of running/stopped state
     */
    private boolean mRunning = false;

    private TextView mElapsedTimeText;
    private TextView mDistanceText;
    private TextView mLatitudeText;
    private TextView mLongitudeText;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_track);

        mModel = TrackerDataModel.getInstance();

        mElapsedTimeText = findViewById(R.id.elapsed_time_value);
        mDistanceText = findViewById(R.id.distance_value);
        mLatitudeText = findViewById(R.id.latitude_value);
        mLongitudeText = findViewById(R.id.longitude_value);


        //
        // Get the Start button and add a click listener to it.
        //
        mStartButton = findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mRunning == false) {

                    startTrack();

                } else {

                    // Set the mRunning false to flag to stop the thread
                    mRunning = false;

                    mModel.stopTrack();
                    //TODO need to ask the user if they want to save the track, etc.
                }
            }
        });

        if (savedInstanceState != null) {

            //
            // There is saved instance data, so we'll restore it and decide what to do.
            //
            int running = savedInstanceState.getInt(KEY_RUNNING, 0);
            if (running != 0) {
                mRunning = true;
                monitorTrack();
            }
        }
    }

    /**
     * Checks to see if the app has permissions to use the location services
     * @return  True if the app has permissions, false if not
     */
    private boolean hasLocationPermissions() {

        int result = ContextCompat.checkSelfPermission(ActiveTrackActivity.this, LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        if (mRunning) {
            savedInstanceState.putInt(KEY_RUNNING, 1);
        } else {
            savedInstanceState.putInt(KEY_RUNNING, 0);
        }

    }

    /**
     * Starts a new track by checking the permissions.  If the app has permission to use the
     * location services, the track will be started.  If not, we'll request permissions.
     */
    private void startTrack() {

        if (hasLocationPermissions()) {

            mModel.startNewTrack(ActiveTrackActivity.this);
            monitorTrack();

        } else {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
        }

    }

    /**
     * Request permissions needed by the app.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int [] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                if (hasLocationPermissions()) {

                    mModel.startNewTrack(ActiveTrackActivity.this);

                    monitorTrack();
                }
        }
    }

    /**
     * Starts the monitoring of an active track, displaying the latest information to the user
     */
    private void monitorTrack() {

        mRunning = true;

        /*
         * Create a thread that will periodically retrieve the latest track info from the data
         * model and display that info.
         */
        new Thread(new Runnable() {
            public void run() {

                do {

                    mElapsedTimeText.post(new Runnable() {
                        public void run() {

                            int elapsedTime = mModel.getElapsedTime();

                            //
                            // Convert the elapsed time (which is in seconds) to
                            // hh:mm:ss format.
                            //
                            int minutes = elapsedTime / 60;
                            int seconds = elapsedTime % 60;
                            int hours = minutes / 60;
                            minutes = minutes % 60;

                            String str = "";

                            if (hours <= 9) {
                                str = str.concat("0");
                            }
                            str = str.concat(Integer.toString(hours));
                            str = str.concat(":");

                            if (minutes <= 9) {
                                str = str.concat("0");
                            }
                            str = str.concat(Integer.toString(minutes));
                            str = str.concat(":");

                            if (seconds <= 9) {
                                str = str.concat("0");
                            }
                            str = str.concat(Integer.toString(seconds));

                            mElapsedTimeText.setText(str);
                        }
                    });

                    mDistanceText.post(new Runnable() {
                        @Override
                        public void run() {
                            mDistanceText.setText(String.format("%d", (int)mModel.getDistanceInFeet()));
                        }
                    });

                    mLatitudeText.post(new Runnable() {
                        @Override
                        public void run() {
                            mLatitudeText.setText(String.format("%8.5f", mModel.getLatitude()));
                        }
                    });

                    mLongitudeText.post(new Runnable() {
                        @Override
                        public void run() {
                            mLongitudeText.setText(String.format("%8.5f", mModel.getLongitude()));
                        }
                    });

                    // Sleep for one second and then do it all again
                    try {
                        Thread.sleep(1000);
                    } catch (java.lang.InterruptedException ie) {

                    }

                } while (mRunning);
            }
        }).start();

        //
        // Change the text on the button to "Stop"
        //
        mStartButton.setText(R.string.stop);

    }
}
