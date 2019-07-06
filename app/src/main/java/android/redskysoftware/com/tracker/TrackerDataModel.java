package android.redskysoftware.com.tracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * Manages all of the data in the Tracker app.
 *
 * Location data is collected by the TrackerLocationService.  That service is started by the
 * TrackerDataModel each time a new track is started.  This class implements the
 * TrackerLocationService.DistanceChangedCallback interface in order to be notified of changes
 * in the location/distance of the current track.  The data model class logs the data.
 *
 * Track data is logged to a text file.  The file is stored in the application's file cache
 * area.
 */
public class TrackerDataModel implements TrackerLocationService.DistanceChangedCallback {

    /** Tag to use to ID Logcat entries */
    private static final String TAG = "TrackerDataModel";

    /** The singleton instance */
    private static TrackerDataModel sModel;

    /** The context the data model is running in */
    private Context mContext;

    /**
     * The service that is providing us track location and distance data.  This is non-null
     * when a track is active
     */
    private TrackerLocationService mLocationService = null;

    /** The output stream used to save data */
    private FileOutputStream mOutputFile = null;

    /** The connection to the TrackerLocationService */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationService = ((TrackerLocationService.LocalBinder)service).getService();
            mLocationService.startLocationCollecting(mContext, TrackerDataModel.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
        }
    };

    /**
     * @return  The singleton instance of the data model, creating a model if one doesn't exist.
     */
    public static TrackerDataModel getInstance() {

        if (sModel == null) {
            sModel = new TrackerDataModel();
        }

        return sModel;
    }

    public float getDistanceInFeet()
    {
        float distance = 0.0f;

        synchronized (this) {
            if (mLocationService != null) {
                distance = mLocationService.getDistance();
            }
        }

        return distance;
    }

    /**
     * @return  The elapsed time of the current track, or 0 if there is no current track in
     *          progress.
     */
    public int getElapsedTime()
    {
        int elapsedTime = 0;

        synchronized (this) {
            if ((mLocationService != null) && (mLocationService != null)) {
                elapsedTime = mLocationService.getElapsedTime();
            }
        }

        return elapsedTime;
    }

    public float getLatitude() {

        float lat = 0.0f;

        synchronized (this) {
            if ((mLocationService != null) && (mLocationService.getCurrentLocation() != null)) {
                lat = (float) mLocationService.getCurrentLocation().getLatitude();
            }
        }

        return lat;
    }

    public float getLongitude() {

        float lon = 0.0f;

        if ((mLocationService != null) && (mLocationService.getCurrentLocation() != null)) {
            lon = (float) mLocationService.getCurrentLocation().getLongitude();
        }

        return lon;
    }

    public void onDistanceChanged(float lat, float lon, float distance) {

        if (mOutputFile != null) {
            try {
                String str = String.format("L,%f,%f,%f\n", lat, lon, distance);
                mOutputFile.write(str.getBytes());
                mOutputFile.flush();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Starts a new track, collecting and storing position data periodically
     * @return  True if the track was started successfully, false if not
     */
    public boolean startNewTrack(Activity activity) {

        mContext = activity;

        File dir = mContext.getFilesDir();
        try {

            /* Construct a file name in year.month.day.hour.minute.txt format */
            Calendar calendar = Calendar.getInstance();
            String filename = String.format("%d.%d.%d.%d.%d.txt", calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH) + 1,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));

            /* Open the file */
            mOutputFile = mContext.openFileOutput(filename, 0);

        } catch (java.io.FileNotFoundException e) {

        }

        /*
         * Bind to the TrackerLocationService.  This will start the service
         */
        Intent i = new Intent(activity, TrackerLocationService.class);
        activity.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);

        return true;
    }

    /**
     * Stops the currently active track.  The elapsed time for the track will be recorded in the
     * output file.  The data model will unbind from the TrackerLocationService.
     */
    public void stopTrack() {

        int elapsedTime = 0;

        if (mLocationService != null) {

            /* Before we let the service go, get the final elapsed time */
            elapsedTime = mLocationService.getElapsedTime();

            /* Unbind from the service and stop it */
            mContext.unbindService(mServiceConnection);

            Intent i = new Intent(mContext, TrackerLocationService.class);
            mContext.stopService(i);
        }

        if (mOutputFile != null) {
            try {
                /* Log the elapsed time in the file */
                String str = String.format("E,%d\n", elapsedTime);
                mOutputFile.write(str.getBytes());
                mOutputFile.flush();
                mOutputFile.close();
            } catch (java.io.IOException ioe) {

            }
        }
    }
}
