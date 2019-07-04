package android.redskysoftware.com.tracker;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;



import java.io.FileOutputStream;


public class TrackerDataModel {



    private static final String TAG = "TrackerDataModel";

    /** The singleton instance */
    private static TrackerDataModel sModel;

    private Context mContext;
    private TrackerLocationService mLocationService = null;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationService = ((TrackerLocationService.LocalBinder)service).getService();
            mLocationService.startLocationCollecting(mContext);
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


    /**
     * Starts a new track, collecting and storing position data periodically
     * @return  True if the track was started successfully, false if not
     */
    public boolean startNewTrack(Activity activity, final FileOutputStream outputStream) {

        Intent i = new Intent(activity, TrackerLocationService.class);
        activity.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);

        mContext = activity;
        return false;
    }

    public void stopTrack() {

        mContext.unbindService(mServiceConnection);

        Intent i = new Intent(mContext, TrackerLocationService.class);
        mContext.stopService(i);
    }
}


// https://guides.codepath.com/android/Retrieving-Location-with-LocationServices-API