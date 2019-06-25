package android.redskysoftware.com.tracker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.FileOutputStream;
import java.lang.Math;
import java.io.IOException;

public class TrackerDataModel {

    /** Meters per degree of longitude at the equator */
    private final Double METERS_PER_DEGREE = 111319.9;

    private static final String TAG = "TrackerDataModel";

    /** The singleton instance */
    private static TrackerDataModel sModel;

    private LocationRequest mLocationRequest;

    private Location mPreviousLocation;
    private Location mCurrentLocation;
    private float mDistanceMeters;
    private boolean mFirstPosition = true;

    private long UPDATE_INTERVAL = 1 * 1000;  /* 5 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private Context mContext;


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
        float distance;

        synchronized (this) {
            distance = mDistanceMeters * 3.28f;
        }

        return distance;
    }

    public float getLatitude() {

        float lat = 0.0f;

        synchronized (this) {
            if (mCurrentLocation != null) {
                lat = (float) mCurrentLocation.getLatitude();
            }
        }

        return lat;
    }

    public float getLongitude() {

        float lon = 0.0f;

        synchronized (this) {
            if (mCurrentLocation != null) {
                lon = (float) mCurrentLocation.getLongitude();
            }
        }

        return lon;
    }

    /**
     * Starts a new track, collecting and storing position data periodically
     * @return  True if the track was started successfully, false if not
     */
    public boolean startNewTrack(Activity activity, final FileOutputStream outputStream) {

        mContext = activity;

        mDistanceMeters = 0;
        mFirstPosition = true;

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(activity);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        try {
            Log.i(TAG, "requesting location");
            LocationServices.getFusedLocationProviderClient(activity).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {

                            // New location is available...
                            synchronized (this) {

                                Location temp = locationResult.getLastLocation();
                                if (temp != null) {

                                    if (!mFirstPosition) {

                                        mCurrentLocation = temp;

                                        float accuracyMeters = 3.0f;

                                        if (mCurrentLocation.hasAccuracy()) {
                                            accuracyMeters = mCurrentLocation.getAccuracy();
                                        }

                                        float deltaMeters = mCurrentLocation.distanceTo(mPreviousLocation);

                                        if (deltaMeters >= accuracyMeters) {
                                            mPreviousLocation = mCurrentLocation;
                                            mDistanceMeters += deltaMeters;
                                        }

                                        //try {
                                        //    String str = String.format("%f %f %f\n",
                                        //            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), deltaMeters);

                                        //    outputStream.write(str.getBytes());
                                        //    outputStream.flush();
                                        //} catch (IOException ioe) {
                                        //    int break_here = 1;
                                        //}

                                    } else {

                                        //
                                        // This is the first location we've received, so set our
                                        // current and previous to this location.
                                        //
                                        mCurrentLocation = temp;
                                        mPreviousLocation = temp;
                                    }
                                }

                                    mFirstPosition = false;
                                //}
                            }
                        }
                    },
                    Looper.myLooper());
        } catch (SecurityException se) {
            int break_here = 1;
        }
        // should we return a track ID instead on success?
        return false;
    }
}


// https://guides.codepath.com/android/Retrieving-Location-with-LocationServices-API